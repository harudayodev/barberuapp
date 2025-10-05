package com.example.barberuapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import com.google.common.util.concurrent.ListenableFuture;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Camera extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private Bitmap capturedBitmap = null;

    private PreviewView previewView;
    private MovableResizableFilter filterOverlay;
    private ImageView capturedImageView;
    private Button captureButton, retakeButton, nextButton;
    private ImageButton rotateButton, returnButton;
    private RecyclerView filtersRecycler;

    // --- New Button Declaration ---
    private ImageButton resetButton;
    // ------------------------------

    private ImageCapture imageCapture;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

    private int selectedFilterId = 0;
    private String customerName;

    private final int[] filterRes = {
            R.drawable.hair1, // short hair
            R.drawable.hair4, // wavy hair left
            R.drawable.hair5, // wavy hair right
            R.drawable.hair3, // curly
            R.drawable.hair2, // messy/other style
            R.drawable.hair6,
            R.drawable.hair7,
            R.drawable.hair8,
            R.drawable.hair9,
            R.drawable.hair10
    };


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        capturedImageView = findViewById(R.id.captured_image_view);
        captureButton = findViewById(R.id.capture_button);
        retakeButton = findViewById(R.id.retake_button);
        nextButton = findViewById(R.id.next_button);
        rotateButton = findViewById(R.id.rotate_button);
        returnButton = findViewById(R.id.return_button);
        filterOverlay = findViewById(R.id.filter_overlay);
        filtersRecycler = findViewById(R.id.filters_recycler);

        // --- Initialize New Button ---
        resetButton = findViewById(R.id.reset_button);
        // -----------------------------

        customerName = getIntent().getStringExtra("fullname");
        if (customerName == null) customerName = "Guest";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }

        setupFilterRecycler();
        setupButtonClickListeners();
    }

    private void setupFilterRecycler() {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        filtersRecycler.setLayoutManager(layoutManager);

        FilterAdapter adapter = new FilterAdapter(filterRes, (resId, position) -> {
            if (selectedFilterId == resId) {
                // Deselect filter
                selectedFilterId = 0;
                filterOverlay.setVisibility(View.GONE);
                resetButton.setVisibility(View.GONE); // Hide the reset button
                ((FilterAdapter) filtersRecycler.getAdapter()).setSelectedPosition(RecyclerView.NO_POSITION);
            } else {
                // Select new filter
                selectedFilterId = resId;
                filterOverlay.setImageResource(resId);
                filterOverlay.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.VISIBLE); // Show the reset button

                // Reset position and scale to original
                resetFilterPosition();

                ((FilterAdapter) filtersRecycler.getAdapter()).setSelectedPosition(position);
            }
        });
        filtersRecycler.setAdapter(adapter);
    }

    // --- New method to reset filter position and size ---
    private void resetFilterPosition() {
        filterOverlay.post(() -> {
            int previewWidth = previewView.getWidth();
            int previewHeight = previewView.getHeight();
            int filterWidth = filterOverlay.getWidth();
            int filterHeight = filterOverlay.getHeight();

            // Reset scale to default
            filterOverlay.setScaleX(1.0f);
            filterOverlay.setScaleY(1.0f);

            // Recalculate and set the center position
            filterOverlay.setX((previewWidth - filterWidth) / 2f);
            filterOverlay.setY((previewHeight - filterHeight) / 2f);
        });
    }
    // ----------------------------------------------------

    private void setupButtonClickListeners() {
        captureButton.setOnClickListener(v -> takePhoto());

        retakeButton.setOnClickListener(v -> {
            capturedImageView.setVisibility(View.GONE);
            previewView.setVisibility(View.VISIBLE);
            if (selectedFilterId != 0) {
                filterOverlay.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.VISIBLE); // Show reset button on retake
            }
            retakeButton.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            captureButton.setVisibility(View.VISIBLE);
            rotateButton.setVisibility(View.VISIBLE);
            returnButton.setVisibility(View.VISIBLE);
            filtersRecycler.setVisibility(View.VISIBLE);
        });

        nextButton.setOnClickListener(v -> {
            if (capturedBitmap != null) {
                saveImageToGallery(capturedBitmap);
            }
            Intent intent = new Intent(Camera.this, HairstyleConfirm.class);
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

        // --- New Button Listener ---
        resetButton.setOnClickListener(v -> {
            resetFilterPosition();
            Toast.makeText(Camera.this, "Filter reset", Toast.LENGTH_SHORT).show();
        });
        // ---------------------------
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
                if (selectedFilterId != 0) {
                    filterOverlay.setVisibility(View.VISIBLE);
                    resetButton.setVisibility(View.VISIBLE);
                } else {
                    resetButton.setVisibility(View.GONE);
                }

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
                        // Apply rotation from camera sensor
                        matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

                        // Flip the image horizontally if the front camera was used to match the preview.
                        if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                            matrix.postScale(-1f, 1f, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
                        }

                        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                                bitmap.getHeight(), matrix, true);

                        // **FIX 1: CROP THE BITMAP TO MATCH THE PREVIEW'S ASPECT RATIO**
                        Bitmap croppedBitmap = cropToMatchPreview(rotatedBitmap);

                        // **FIX 2: MERGE WITH OVERLAY**
                        capturedBitmap = mergeWithOverlay(croppedBitmap);

                        runOnUiThread(() -> {
                            previewView.setVisibility(View.GONE);
                            filterOverlay.setVisibility(View.GONE);
                            resetButton.setVisibility(View.GONE); // Hide the reset button after capture
                            capturedImageView.setVisibility(View.VISIBLE);
                            capturedImageView.setImageBitmap(capturedBitmap);

                            captureButton.setVisibility(View.GONE);
                            rotateButton.setVisibility(View.GONE);
                            returnButton.setVisibility(View.GONE);
                            filtersRecycler.setVisibility(View.GONE);
                            retakeButton.setVisibility(View.VISIBLE);
                            nextButton.setVisibility(View.VISIBLE);
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(Camera.this,
                                "Capture failed: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Bitmap cropToMatchPreview(Bitmap photo) {
        int photoWidth = photo.getWidth();
        int photoHeight = photo.getHeight();
        float photoRatio = (float) photoWidth / photoHeight;

        int previewWidth = previewView.getWidth();
        int previewHeight = previewView.getHeight();
        float previewRatio = (float) previewWidth / previewHeight;

        int newWidth = photoWidth;
        int newHeight = photoHeight;
        int startX = 0;
        int startY = 0;

        if (photoRatio > previewRatio) {
            newWidth = (int) (photoHeight * previewRatio);
            startX = (photoWidth - newWidth) / 2;
        } else if (previewRatio > photoRatio) {
            newHeight = (int) (photoWidth / previewRatio);
            startY = (photoHeight - newHeight) / 2;
        }

        return Bitmap.createBitmap(photo, startX, startY, newWidth, newHeight);
    }

    private Bitmap mergeWithOverlay(Bitmap photo) {
        if (selectedFilterId == 0) {
            return photo;
        }

        Bitmap filterBitmap = BitmapFactory.decodeResource(getResources(), selectedFilterId);

        int photoWidth = photo.getWidth();
        int photoHeight = photo.getHeight();

        float overlayX = filterOverlay.getX();
        float overlayY = filterOverlay.getY();
        float overlayW = filterOverlay.getWidth() * filterOverlay.getScaleX();
        float overlayH = filterOverlay.getHeight() * filterOverlay.getScaleY();

        int previewWidth = previewView.getWidth();
        int previewHeight = previewView.getHeight();

        float scaleX = (float) photoWidth / previewWidth;
        float scaleY = (float) photoHeight / previewHeight;

        float newFilterX = overlayX * scaleX;
        float newFilterY = overlayY * scaleY;
        float newFilterW = overlayW * scaleX;
        float newFilterH = overlayH * scaleY;

        Bitmap scaledFilter = Bitmap.createScaledBitmap(
                filterBitmap,
                Math.round(newFilterW),
                Math.round(newFilterH),
                true
        );

        Bitmap result = Bitmap.createBitmap(photoWidth, photoHeight, photo.getConfig());
        Canvas canvas = new Canvas(result);

        canvas.drawBitmap(photo, 0, 0, null);
        canvas.drawBitmap(scaledFilter, newFilterX, newFilterY, null);

        return result;
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
                    Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_LONG).show();
            }
        }
    }
}