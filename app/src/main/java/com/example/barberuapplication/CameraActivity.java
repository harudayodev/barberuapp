package com.example.barberuapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.ImageFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private Bitmap capturedBitmap = null;

    private PreviewView previewView;
    private ImageView capturedImageView;
    private Button captureButton, retakeButton, nextButton;
    private ImageButton rotateButton, returnButton;
    private HorizontalScrollView filtersScrollView;
    private ImageView filter1, filter2, filter3, filter4;
    private Toast filterToast;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

    private int selectedFilterId = 0;
    private String customerName;

    private YuvToRgbConverter yuvToRgbConverter;

    @Override
    public void onBackPressed() {
        if (capturedImageView.getVisibility() == ImageView.VISIBLE) {
            capturedImageView.setVisibility(ImageView.GONE);
            previewView.setVisibility(PreviewView.VISIBLE);

            retakeButton.setVisibility(Button.GONE);
            nextButton.setVisibility(Button.GONE);
            captureButton.setVisibility(Button.VISIBLE);
            rotateButton.setVisibility(ImageButton.VISIBLE);
            returnButton.setVisibility(ImageButton.VISIBLE);
            filtersScrollView.setVisibility(HorizontalScrollView.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        capturedImageView = findViewById(R.id.captured_image_view);
        captureButton = findViewById(R.id.capture_button);
        retakeButton = findViewById(R.id.retake_button);
        nextButton = findViewById(R.id.next_button);
        rotateButton = findViewById(R.id.rotate_button);
        returnButton = findViewById(R.id.return_button);
        filtersScrollView = findViewById(R.id.filters_scroll_view);
        filter1 = findViewById(R.id.filter1);
        filter2 = findViewById(R.id.filter2);
        filter3 = findViewById(R.id.filter3);
        filter4 = findViewById(R.id.filter4);

        cameraExecutor = Executors.newSingleThreadExecutor();
        customerName = getIntent().getStringExtra("fullname");
        if (customerName == null) customerName = "Guest";

        yuvToRgbConverter = new YuvToRgbConverter(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }

        captureButton.setOnClickListener(v -> takePhoto());
        retakeButton.setOnClickListener(v -> {
            capturedImageView.setVisibility(ImageView.GONE);
            previewView.setVisibility(PreviewView.VISIBLE);
            retakeButton.setVisibility(Button.GONE);
            nextButton.setVisibility(Button.GONE);
            captureButton.setVisibility(Button.VISIBLE);
            rotateButton.setVisibility(ImageButton.VISIBLE);
            returnButton.setVisibility(ImageButton.VISIBLE);
            filtersScrollView.setVisibility(HorizontalScrollView.VISIBLE);
        });

        nextButton.setOnClickListener(v -> {
            if (capturedBitmap != null) {
                saveImageToGallery(capturedBitmap);
            }
            Intent intent = new Intent(CameraActivity.this, HairstyleConfirm.class);
            intent.putExtra("customername", customerName);
            startActivity(intent);
        });

        rotateButton.setOnClickListener(v -> {
            RotateAnimation rotateAnimation = new RotateAnimation(
                    0, 180,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f
            );
            rotateAnimation.setDuration(500);
            v.startAnimation(rotateAnimation);

            cameraSelector = (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                    ? CameraSelector.DEFAULT_FRONT_CAMERA
                    : CameraSelector.DEFAULT_BACK_CAMERA;
            startCamera();
        });

        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(CameraActivity.this, HomepageActivity.class);
            startActivity(intent);
            finish();
        });

        filter1.setOnClickListener(v -> handleFilterClick(v.getId()));
        filter2.setOnClickListener(v -> handleFilterClick(v.getId()));
        filter3.setOnClickListener(v -> handleFilterClick(v.getId()));
        filter4.setOnClickListener(v -> handleFilterClick(v.getId()));
    }

    private void handleFilterClick(int filterId) {
        // Remove outline from previous selection
        clearFilterSelection();

        if (selectedFilterId == filterId) {
            selectedFilterId = 0;
            showFilterToast("Filter deselected.");
        } else {
            selectedFilterId = filterId;
            showFilterToast("Filter selected.");

            // Highlight selected filter
            ImageView selectedFilter = findViewById(filterId);
            selectedFilter.setBackgroundResource(R.drawable.filter_active);

            // Animate selection
            animateFilterSelection(selectedFilter);
        }
    }

    private void showFilterToast(String message) {
        if (filterToast != null) filterToast.cancel();
        filterToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        filterToast.show();
    }

    private void clearFilterSelection() {
        ImageView[] filters = {filter1, filter2, filter3, filter4};
        for (ImageView filter : filters) {
            filter.setBackground(null);
            filter.setScaleX(1f);
            filter.setScaleY(1f);
        }
    }
    private void animateFilterSelection(ImageView filter) {
        filter.setScaleX(0.9f);
        filter.setScaleY(0.9f);
        filter.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(150)
                .start();
    }

    private void animateOutline(ImageView filter) {
        filter.setBackgroundResource(R.drawable.filter_active);
        filter.getBackground().setAlpha(0); // start invisible

        filter.animate()
                .alpha(1f) // fade-in effect
                .setDuration(200)
                .start();
    }


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                cameraProvider.bindToLifecycle(
                        (LifecycleOwner) this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        Bitmap bitmap = imageProxyToBitmap(imageProxy);

                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        Matrix matrix = new Matrix();
                        matrix.postRotate(rotationDegrees);

                        if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                            matrix.postScale(-1f, 1f);
                        }

                        capturedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                        runOnUiThread(() -> {
                            previewView.setVisibility(PreviewView.GONE);
                            capturedImageView.setVisibility(ImageView.VISIBLE);
                            capturedImageView.setImageBitmap(capturedBitmap);

                            captureButton.setVisibility(Button.GONE);
                            rotateButton.setVisibility(ImageButton.GONE);
                            returnButton.setVisibility(ImageButton.GONE);
                            filtersScrollView.setVisibility(HorizontalScrollView.GONE);
                            retakeButton.setVisibility(Button.VISIBLE);
                            nextButton.setVisibility(Button.VISIBLE);
                        });

                        imageProxy.close();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CameraActivity.this,
                                "Capture failed: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        if (image.getFormat() == ImageFormat.JPEG) {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            Bitmap bitmap = Bitmap.createBitmap(
                    image.getWidth(),
                    image.getHeight(),
                    Bitmap.Config.ARGB_8888
            );
            yuvToRgbConverter.yuvToRgb(image, bitmap);
            return bitmap;
        }
    }

    private void saveImageToGallery(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME,
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()));
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
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

    static class YuvToRgbConverter {
        private final android.renderscript.RenderScript rs;
        private final android.renderscript.ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
        private android.renderscript.Allocation in, out;
        private android.renderscript.Type.Builder yuvType, rgbaType;

        public YuvToRgbConverter(Context context) {
            rs = android.renderscript.RenderScript.create(context);
            yuvToRgbIntrinsic = android.renderscript.ScriptIntrinsicYuvToRGB.create(rs, android.renderscript.Element.U8(rs));
        }

        public void yuvToRgb(ImageProxy image, Bitmap output) {
            ByteBuffer yuvBuffer = image.getPlanes()[0].getBuffer();
            byte[] yuvBytes = new byte[yuvBuffer.remaining()];
            yuvBuffer.get(yuvBytes);

            if (yuvType == null) {
                yuvType = new android.renderscript.Type.Builder(rs, android.renderscript.Element.U8(rs))
                        .setX(yuvBytes.length);
                in = android.renderscript.Allocation.createTyped(rs, yuvType.create(), android.renderscript.Allocation.USAGE_SCRIPT);

                rgbaType = new android.renderscript.Type.Builder(rs, android.renderscript.Element.RGBA_8888(rs))
                        .setX(image.getWidth())
                        .setY(image.getHeight());
                out = android.renderscript.Allocation.createTyped(rs, rgbaType.create(), android.renderscript.Allocation.USAGE_SCRIPT);
            }

            in.copyFrom(yuvBytes);
            yuvToRgbIntrinsic.setInput(in);
            yuvToRgbIntrinsic.forEach(out);
            out.copyTo(output);
        }
    }
}
