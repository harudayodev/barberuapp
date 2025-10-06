package com.example.barberuapplication;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DbHelper {

    public interface DBCallback {
        void onResult(String status, String message, JSONObject data);
    }

    // REGISTER METHOD
    public void registerUser(String fname, String lname, String email, String password, DBCallback callback) {
        new RegisterTask(fname, lname, email, password, callback).execute();
    }

    // LOGIN METHOD
    public void loginUser(String email, String password, DBCallback callback) {
        new LoginTask(email, password, callback).execute();
    }

    // ---------------- REGISTER TASK ----------------
    private static class RegisterTask extends AsyncTask<Void, Void, String> {
        private final String fname, lname, email, password;
        private final DBCallback callback;

        RegisterTask(String fname, String lname, String email, String password, DBCallback callback) {
            this.fname = fname;
            this.lname = lname;
            this.email = email;
            this.password = password;
            this.callback = callback;
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Config.BASE_URL + "register.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));

                String data = URLEncoder.encode("firstname", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(fname, StandardCharsets.UTF_8) + "&" +
                        URLEncoder.encode("lastname", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(lname, StandardCharsets.UTF_8) + "&" +
                        URLEncoder.encode("email", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&" +
                        URLEncoder.encode("password", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
                return sb.toString();

            } catch (Exception e) {
                Log.e("RegisterTask", "Error during registration: " + e.getMessage());
                return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
            } finally {
                if (conn != null) conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json = new JSONObject(result);
                String status = json.optString("status", "error");
                String message = json.optString("message", "Unknown error");
                callback.onResult(status, message, json);
            } catch (Exception e) {
                callback.onResult("error", "Invalid response from server", null);
            }
        }
    }

    // ---------------- LOGIN TASK ----------------
    private static class LoginTask extends AsyncTask<Void, Void, String> {
        private final String email, password;
        private final DBCallback callback;

        LoginTask(String email, String password, DBCallback callback) {
            this.email = email;
            this.password = password;
            this.callback = callback;
        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(Config.BASE_URL + "login.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));

                String data = URLEncoder.encode("email", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&" +
                        URLEncoder.encode("password", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
                return sb.toString();

            } catch (Exception e) {
                Log.e("LoginTask", "Error during login: " + e.getMessage());
                return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
            } finally {
                if (conn != null) conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json = new JSONObject(result);
                String status = json.optString("status", "error");
                String message = json.optString("message", "Unknown error");
                callback.onResult(status, message, json);
            } catch (Exception e) {
                callback.onResult("error", "Invalid response from server", null);
            }
        }
    }
}
