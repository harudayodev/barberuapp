package com.example.barberuapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Camera extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private PreviewView previewView;
    private ImageView capturedImageView;
    private RecyclerView filterList;
    private Button captureButton, retakeButton, nextButton;
    private ImageButton rotateButton, returnButton;
    private ProgressBar loadingSpinner;
    private TextView selectedHaircutLabel;

    private ImageCapture imageCapture;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

    private Bitmap capturedBitmap = null;
    private String customerName;
    private FilterAdapter filterAdapter;
    private int selectedFilterPosition = RecyclerView.NO_POSITION;

    // Reusable Toast
    private Toast activeToast;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        capturedImageView = findViewById(R.id.captured_image_view);
        filterList = findViewById(R.id.filter_list);
        captureButton = findViewById(R.id.capture_button);
        retakeButton = findViewById(R.id.retake_button);
        nextButton = findViewById(R.id.next_button);
        rotateButton = findViewById(R.id.rotate_button);
        returnButton = findViewById(R.id.return_button);
        loadingSpinner = findViewById(R.id.loading_spinner);
        selectedHaircutLabel = findViewById(R.id.selected_haircut_label);

        customerName = getIntent().getStringExtra("fullname");
        if (customerName == null) customerName = "Guest";

        setupFilterList();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }

        setupButtonClickListeners();
    }

    // 🔹 Custom Styled Toast (semi-transparent bubble above filter bar)
    private void showStyledToast(String message) {
        if (activeToast != null) activeToast.cancel();

        TextView toastText = new TextView(this);
        toastText.setText(message);
        toastText.setTextColor(Color.WHITE);
        toastText.setTextSize(15);
        toastText.setPadding(35, 20, 35, 20);
        toastText.setGravity(Gravity.CENTER);

        // Background: rounded corners + semi-transparent dark gray
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#AA000000")); // 67% opaque black
        bg.setCornerRadius(50);
        toastText.setBackground(bg);

        // Wrap in layout (to center horizontally)
        LinearLayout layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        layout.addView(toastText);

        activeToast = new Toast(this);
        activeToast.setView(layout);
        activeToast.setDuration(Toast.LENGTH_SHORT);
        activeToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 250);
        activeToast.show();
    }

    @SuppressLint("SetTextI18n")
    private void setupFilterList() {
        filterList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Haircut> haircutList = new ArrayList<>();
        filterAdapter = new FilterAdapter(haircutList, (haircut, position) -> {
            if (selectedFilterPosition == position) {
                showStyledToast(haircut.getName() + " deselected");
                selectedFilterPosition = RecyclerView.NO_POSITION;
                filterAdapter.setSelectedPosition(RecyclerView.NO_POSITION);
                selectedHaircutLabel.setText("No haircut selected");
            } else {
                showStyledToast(haircut.getName() + " selected");
                selectedFilterPosition = position;
                filterAdapter.setSelectedPosition(position);
                selectedHaircutLabel.setText("Selected: " + haircut.getName());
            }
        });
        filterList.setAdapter(filterAdapter);

        fetchHaircutsFromServer(haircutList);
    }

    private void fetchHaircutsFromServer(List<Haircut> haircutList) {
        String url = Config.BASE_URL + "get_haircuts_filters.php";
        loadingSpinner.setVisibility(View.VISIBLE);
        filterList.setVisibility(View.INVISIBLE);
        selectedHaircutLabel.setVisibility(View.INVISIBLE);

        RequestQueue queue = Volley.newRequestQueue(this);
        @SuppressLint("NotifyDataSetChanged") JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray haircutsArray = response.getJSONArray("haircuts");
                        for (int i = 0; i < haircutsArray.length(); i++) {
                            JSONObject obj = haircutsArray.getJSONObject(i);
                            haircutList.add(new Haircut(
                                    obj.getString("id"),
                                    obj.getString("name")
                            ));
                        }
                        filterAdapter.notifyDataSetChanged();
                        loadingSpinner.setVisibility(View.GONE);
                        filterList.setVisibility(View.VISIBLE);
                        selectedHaircutLabel.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        showStyledToast("Error parsing haircut data.");
                        loadingSpinner.setVisibility(View.GONE);
                    }
                },
                error -> {
                    showStyledToast("Failed to load haircuts. Please try again.");
                    loadingSpinner.setVisibility(View.GONE);
                });

        queue.add(request);
    }

    private void setupButtonClickListeners() {
        captureButton.setOnClickListener(v -> takePhoto());

        retakeButton.setOnClickListener(v -> {
            capturedImageView.setVisibility(View.GONE);
            previewView.setVisibility(View.VISIBLE);
            retakeButton.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            captureButton.setVisibility(View.VISIBLE);
            rotateButton.setVisibility(View.VISIBLE);
            returnButton.setVisibility(View.VISIBLE);
            filterList.setVisibility(View.VISIBLE);
            selectedHaircutLabel.setVisibility(View.VISIBLE);
        });

        nextButton.setOnClickListener(v -> {
            if (capturedBitmap != null) {
                saveImageToGallery(capturedBitmap);
            }
            Intent intent = new Intent(Camera.this, BarberShopStorePicker.class);
            intent.putExtra("customername", customerName);
            startActivity(intent);
        });

        rotateButton.setOnClickListener(v -> {
            cameraSelector = (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                    ? CameraSelector.DEFAULT_FRONT_CAMERA
                    : CameraSelector.DEFAULT_BACK_CAMERA;
            startCamera();
        });

        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(Camera.this, HomepageActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();

                androidx.camera.core.Preview preview = new androidx.camera.core.Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        Bitmap bitmap = imageProxyToBitmap(imageProxy);
                        imageProxy.close();

                        Matrix matrix = new Matrix();
                        matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

                        if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                            matrix.postScale(-1f, 1f, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
                        }

                        Bitmap rotatedBitmap = Bitmap.createBitmap(
                                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                        capturedBitmap = rotatedBitmap;

                        runOnUiThread(() -> {
                            previewView.setVisibility(View.GONE);
                            capturedImageView.setVisibility(View.VISIBLE);
                            capturedImageView.setImageBitmap(capturedBitmap);

                            captureButton.setVisibility(View.GONE);
                            rotateButton.setVisibility(View.GONE);
                            returnButton.setVisibility(View.GONE);
                            filterList.setVisibility(View.GONE);
                            selectedHaircutLabel.setVisibility(View.GONE);
                            retakeButton.setVisibility(View.VISIBLE);
                            nextButton.setVisibility(View.VISIBLE);
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        showStyledToast("Capture failed: " + exception.getMessage());
                    }
                });
    }

    private void saveImageToGallery(Bitmap bitmap) {
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    showStyledToast("Image saved!");
                }
            } catch (Exception e) {
                showStyledToast("Save failed: " + e.getMessage());
            }
        }
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                showStyledToast("Camera permission denied.");
            }
        }
    }
}
