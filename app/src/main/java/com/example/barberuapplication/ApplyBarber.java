package com.example.barberuapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
// ProgressDialog import removed as it's not used
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplyBarber extends AppCompatActivity {

    // (Your existing class members are correct)
    EditText fnameInput, lnameInput, contactInput, addressInput, emailInput;
    Button returnBtn, resumeUploadBtn, submitBtn;
    ImageButton clearResumeBtn;
    Spinner barbershopSpinner;
    private List<SpinnerBarbershop> barbershopList;
    private ArrayAdapter<SpinnerBarbershop> spinnerArrayAdapter;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_barber);

        // (Your existing onCreate code is correct)
        fnameInput = findViewById(R.id.fname_input);
        lnameInput = findViewById(R.id.lname_input);
        contactInput = findViewById(R.id.contact_input);
        addressInput = findViewById(R.id.address_input);
        emailInput = findViewById(R.id.email_input);
        returnBtn = findViewById(R.id.return_btn);
        barbershopSpinner = findViewById(R.id.barbershop_spinner);
        resumeUploadBtn = findViewById(R.id.resume_upload_btn);
        clearResumeBtn = findViewById(R.id.clear_resume_btn);
        submitBtn = findViewById(R.id.submit_btn);
        setupContactInput();
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        String fileName = getFileNameFromUri(selectedImageUri);
                        updateResumeButton(fileName);
                    }
                });
        returnBtn.setOnClickListener(v -> handleReturn());
        clearResumeBtn.setOnClickListener(v -> clearSelectedImage());
        resumeUploadBtn.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                showImagePreview();
            } else {
                openGallery();
            }
        });
        submitBtn.setOnClickListener(v -> handleSubmit());
        setupSpinner();
        fetchBarbershops();
    }

    private void uploadApplicationData() {
        String url = Config.BASE_URL + "submit_application.php";

        String resumeBase64 = convertUriToBase64(selectedImageUri);
        if (resumeBase64 == null) {
            showToast("Could not process resume image.");
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Log the raw server response to help with debugging
                    Log.d("ApplyBarber", "Server Response: " + response);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");

                        // --- THIS IS THE FIX ---
                        // Use equalsIgnoreCase to handle "success" or "Success"
                        if ("success".equalsIgnoreCase(status)) {
                            String fullName = fnameInput.getText().toString().trim() + " " + lnameInput.getText().toString().trim();
                            showSuccessDialog(fullName);
                        } else {
                            String message = jsonObject.getString("message");
                            showToast(message);
                        }
                    } catch (JSONException e) {
                        Log.e("ApplyBarber", "JSON parsing error: " + e.getMessage());
                        showToast("Error processing server response.");
                    }
                },
                error -> {
                    Log.e("ApplyBarber", "Volley error: " + error.toString());
                    showToast("Submission failed. Check your connection.");
                }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                SpinnerBarbershop selectedShop = (SpinnerBarbershop) barbershopSpinner.getSelectedItem();
                Map<String, String> params = new HashMap<>();
                params.put("app_firstname", fnameInput.getText().toString().trim());
                params.put("app_lastname", lnameInput.getText().toString().trim());
                params.put("app_contact", contactInput.getText().toString().trim());
                params.put("app_address", addressInput.getText().toString().trim());
                params.put("app_emailadd", emailInput.getText().toString().trim());
                params.put("shopID", selectedShop.getShopID());
                params.put("app_resume", resumeBase64);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(stringRequest);
    }

    // (The rest of your Java code is correct and does not need changes)
    @SuppressLint("SetTextI18n")
    private void showSuccessDialog(String fullname) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_register_success, null);
        TextView messageText = dialogView.findViewById(R.id.message_text);
        messageText.setText("Thank you, " + fullname + "!\nYour application has been submitted.");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().getAttributes().windowAnimations = R.style.FadeDialogAnimation;
        }
        dialog.show();
        dialogView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bounce));
        dialogView.postDelayed(() -> {
            dialog.dismiss();
            finish();
        }, 3500);
    }
    private void setupContactInput() { contactInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)}); contactInput.addTextChangedListener(new TextWatcher() { private boolean isUpdating = false; @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {} @Override public void onTextChanged(CharSequence s, int start, int before, int count) {} @Override public void afterTextChanged(Editable s) { if (isUpdating) return; if (!s.toString().startsWith("+63")) { isUpdating = true; contactInput.setText("+63"); contactInput.setSelection(contactInput.getText().length()); isUpdating = false; } } }); contactInput.setText("+63"); }
    private void handleSubmit() { if (validateInputs()) { uploadApplicationData(); } }
    private String convertUriToBase64(Uri uri) { try { InputStream inputStream = getContentResolver().openInputStream(uri); byte[] bytes = getBytes(inputStream); return Base64.encodeToString(bytes, Base64.DEFAULT); } catch (IOException e) { e.printStackTrace(); return null; } }
    private byte[] getBytes(InputStream inputStream) throws IOException { ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream(); int bufferSize = 1024; byte[] buffer = new byte[bufferSize]; int len; while ((len = inputStream.read(buffer)) != -1) { byteBuffer.write(buffer, 0, len); } return byteBuffer.toByteArray(); }
    private boolean validateInputs() { String fname = fnameInput.getText().toString().trim(); String lname = lnameInput.getText().toString().trim(); String contact = contactInput.getText().toString().trim(); String address = addressInput.getText().toString().trim(); String email = emailInput.getText().toString().trim(); int selectedShopPosition = barbershopSpinner.getSelectedItemPosition(); if (fname.isEmpty()) { showToast("First name is required."); return false; } if (lname.isEmpty()) { showToast("Last name is required."); return false; } if (contact.length() != 13) { showToast("Contact number must have 10 digits after +63."); return false; } if (address.isEmpty()) { showToast("Address is required."); return false; } if (email.isEmpty()) { showToast("Email address is required."); return false; } if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { showToast("Please enter a valid email address."); return false; } if (!email.toLowerCase().endsWith("@gmail.com")) { showToast("Only @gmail.com email addresses are accepted."); return false; } if (selectedShopPosition == 0) { showToast("Please select a barbershop."); return false; } if (selectedImageUri == null) { showToast("Please upload a resume image."); return false; } return true; }
    private void showToast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
    private void showImagePreview() { if (selectedImageUri == null) return; final Dialog dialog = new Dialog(this); dialog.setContentView(R.layout.dialog_image_preview); if (dialog.getWindow() != null) { dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); } ImageView previewImageView = dialog.findViewById(R.id.preview_image_view); previewImageView.setImageURI(selectedImageUri); dialog.setCancelable(true); dialog.show(); }
    @SuppressLint("SetTextI18n") private void clearSelectedImage() { selectedImageUri = null; resumeUploadBtn.setText("Upload Resume (Image)"); resumeUploadBtn.setTextColor(ContextCompat.getColor(this, R.color.light_gray)); clearResumeBtn.setVisibility(View.GONE); }
    private void updateResumeButton(String fileName) { resumeUploadBtn.setText(fileName); resumeUploadBtn.setTextColor(ContextCompat.getColor(this, R.color.white)); clearResumeBtn.setVisibility(View.VISIBLE); showToast("Selected: " + fileName); }
    private void openGallery() { Intent intent = new Intent(Intent.ACTION_GET_CONTENT); intent.setType("image/*"); imagePickerLauncher.launch(intent); }
    @SuppressLint("Range") private String getFileNameFromUri(Uri uri) { String fileName = null; try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) { if (cursor != null && cursor.moveToFirst()) { fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)); } } if (fileName == null) { fileName = uri.getPath(); if (fileName != null) { int cut = fileName.lastIndexOf('/'); if (cut != -1) { fileName = fileName.substring(cut + 1); } } } return fileName; }
    private void setupSpinner() { barbershopList = new ArrayList<>(); spinnerArrayAdapter = new ArrayAdapter<SpinnerBarbershop>( this, android.R.layout.simple_spinner_item, barbershopList) { @Override public boolean isEnabled(int position) { return position != 0; } @Override public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) { View view = super.getDropDownView(position, convertView, parent); TextView textView = (TextView) view; if (position == 0) { textView.setTextColor(ContextCompat.getColor(getContext(), R.color.light_gray)); } else { textView.setTextColor(Color.BLACK); } return view; } @NonNull @Override public View getView(int position, View convertView, @NonNull ViewGroup parent) { View view = super.getView(position, convertView, parent); TextView textView = (TextView) view; if (position == 0) { textView.setTextColor(ContextCompat.getColor(getContext(), R.color.light_gray)); } else { textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white)); } return view; } }; spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); barbershopSpinner.setAdapter(spinnerArrayAdapter); barbershopSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {} @Override public void onNothingSelected(AdapterView<?> parent) {} }); }
    private void fetchBarbershops() { String url = Config.BASE_URL + "get_barbershops.php"; RequestQueue queue = Volley.newRequestQueue(this); JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, response -> { barbershopList.clear(); barbershopList.add(new SpinnerBarbershop(null, "Pick barbershop to apply on")); try { for (int i = 0; i < response.length(); i++) { JSONObject shopObject = response.getJSONObject(i); String shopID = shopObject.getString("shopID"); String shopName = shopObject.getString("name"); barbershopList.add(new SpinnerBarbershop(shopID, shopName)); } } catch (JSONException e) { Log.e("ApplyBarber", "JSON parsing error: " + e.getMessage()); showToast("Error parsing data"); } spinnerArrayAdapter.notifyDataSetChanged(); }, error -> { Log.e("ApplyBarber", "Volley error: " + error.getMessage()); showToast("Failed to load barbershops"); }); queue.add(jsonArrayRequest); }
    private void handleReturn() { if (hasInput() || selectedImageUri != null) { new AlertDialog.Builder(this) .setTitle("Unsaved Changes") .setMessage("Are you sure you want to return?\nAll input will be lost.") .setPositiveButton("Yes", (dialog, which) -> finish()) .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) .show(); } else { finish(); } }
    private boolean hasInput() { return !fnameInput.getText().toString().trim().isEmpty() || !lnameInput.getText().toString().trim().isEmpty() || !contactInput.getText().toString().trim().isEmpty() || !addressInput.getText().toString().trim().isEmpty() || !emailInput.getText().toString().trim().isEmpty(); }
}