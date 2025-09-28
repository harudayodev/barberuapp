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
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
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
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

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
    private ImageView capturedImageView, filterOverlay;
    private Button captureButton, retakeButton, nextButton;
    private ImageButton rotateButton, returnButton;
    private HorizontalScrollView filtersScrollView;
    private ImageView filter1, filter2, filter3, filter4;

    private ImageCapture imageCapture;
    private FaceDetector faceDetector;
    private ExecutorService cameraExecutor;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

    private int selectedFilterId = 0;
    private String customerName;

    private YuvToRgbConverter yuvToRgbConverter;

    @Override
    public void onBackPressed() {
        // If the captured image is visible (preview/retake screen)
        if (capturedImageView.getVisibility() == ImageView.VISIBLE) {
            // Go back to camera preview
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
        filterOverlay = findViewById(R.id.filter_overlay);
        filter1 = findViewById(R.id.filter1);
        filter2 = findViewById(R.id.filter2);
        filter3 = findViewById(R.id.filter3);
        filter4 = findViewById(R.id.filter4);

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .enableTracking()
                .build();
        faceDetector = FaceDetection.getClient(options);

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

    private void saveImageToGallery(Bitmap bitmap) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME,
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()));
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show();  // 🔥 updated message
        } catch (Exception e) {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFilterClick(int filterId) {
        if (selectedFilterId == filterId) {
            selectedFilterId = 0;
            filterOverlay.setVisibility(ImageView.GONE);
            Toast.makeText(this, "Filter deselected.", Toast.LENGTH_SHORT).show();
        } else {
            selectedFilterId = filterId;
            filterOverlay.setVisibility(ImageView.VISIBLE);
            if (filterId == R.id.filter1) {
                filterOverlay.setImageResource(R.drawable.hair);
            }
        }
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

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                Camera camera = cameraProvider.bindToLifecycle(
                        (LifecycleOwner) this,
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalysis
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeImage(ImageProxy imageProxy) {
        @SuppressWarnings("UnsafeOptInUsageError")
        InputImage inputImage =
                InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        int frameWidth = imageProxy.getWidth();
        int frameHeight = imageProxy.getHeight();

        faceDetector.process(inputImage)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty() && selectedFilterId != 0) {
                        runOnUiThread(() -> updateFilterOverlay(faces.get(0), frameWidth, frameHeight));
                    } else {
                        runOnUiThread(() -> filterOverlay.setVisibility(ImageView.GONE));
                    }
                    imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    imageProxy.close();
                });
    }

    private float[] mapImageCoordinatesToView(float x, float y, int imageWidth, int imageHeight) {
        int viewWidth = previewView.getWidth();
        int viewHeight = previewView.getHeight();

        float scaleX = (float) viewWidth / imageWidth;
        float scaleY = (float) viewHeight / imageHeight;

        float scaleFactor = Math.min(scaleX, scaleY);

        float offsetX = (viewWidth - imageWidth * scaleFactor) / 2.0f;
        float offsetY = (viewHeight - imageHeight * scaleFactor) / 2.0f;

        float mappedX = x * scaleFactor + offsetX;
        float mappedY = y * scaleFactor + offsetY;

        return new float[]{mappedX, mappedY};
    }

    private void updateFilterOverlay(Face face, int imageWidth, int imageHeight) {
        int faceBoundingBoxWidth = face.getBoundingBox().width();
        int faceBoundingBoxHeight = face.getBoundingBox().height();

        float desiredOverlayWidth = faceBoundingBoxWidth * 1.6f;
        float desiredOverlayHeight = faceBoundingBoxHeight * 0.8f;

        float frameX = face.getBoundingBox().centerX() - desiredOverlayWidth / 2f;
        float frameY = face.getBoundingBox().top - (faceBoundingBoxHeight * 0.25f);

        float[] mappedTopLeft = mapImageCoordinatesToView(frameX, frameY, imageWidth, imageHeight);
        float[] mappedBottomRight = mapImageCoordinatesToView(
                frameX + desiredOverlayWidth,
                frameY + desiredOverlayHeight,
                imageWidth,
                imageHeight
        );

        float mappedX = mappedTopLeft[0];
        float mappedY = mappedTopLeft[1];
        int mappedWidth = (int) (mappedBottomRight[0] - mappedTopLeft[0]);
        int mappedHeight = (int) (mappedBottomRight[1] - mappedTopLeft[1]);

        filterOverlay.setVisibility(ImageView.VISIBLE);

        filterOverlay.getLayoutParams().width = mappedWidth;
        filterOverlay.getLayoutParams().height = mappedHeight;
        filterOverlay.requestLayout();

        filterOverlay.setX(mappedX);
        filterOverlay.setY(mappedY);

        if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            float mirroredX = previewView.getWidth() - (mappedX + mappedWidth);
            filterOverlay.setX(mirroredX);
        } else {
            filterOverlay.setX(mappedX);
        }
        filterOverlay.setY(mappedY);

        float rotation = face.getHeadEulerAngleZ();
        if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            rotation = -rotation;
        }
        filterOverlay.setRotation(rotation);
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        Bitmap bitmap = imageProxyToBitmap(imageProxy);

                        // Get the rotation from the ImageProxy
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        Matrix matrix = new Matrix();

                        // Apply rotation to the bitmap
                        matrix.postRotate(rotationDegrees);

                        // Mirror the image for the front camera
                        if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                            matrix.postScale(-1f, 1f);
                        }

                        // Create the final, correctly oriented bitmap
                        Bitmap finalBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                bitmap.getWidth(), bitmap.getHeight(),
                                matrix, true);

                        capturedBitmap = finalBitmap;

                        runOnUiThread(() -> {
                            previewView.setVisibility(PreviewView.GONE);
                            capturedImageView.setVisibility(ImageView.VISIBLE);
                            capturedImageView.setImageBitmap(finalBitmap);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (faceDetector != null) faceDetector.close();
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