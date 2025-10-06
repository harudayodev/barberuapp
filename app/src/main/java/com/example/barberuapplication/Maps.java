package com.example.barberuapplication;

import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Maps extends AppCompatActivity {

    private ZoomableImageView zoomableImageView;
    private Button goToButton;
    private String currentShopLink = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_maps);

        ImageView returnbutton = findViewById(R.id.returnbutton);
        ImageView homebutton = findViewById(R.id.homeview);
        View mapBorder = findViewById(R.id.map_border);
        zoomableImageView = findViewById(R.id.mapactual);
        Button resetButton = findViewById(R.id.reset_button);
        goToButton = findViewById(R.id.go_to_button);

        Animation pulsingAnimation = AnimationUtils.loadAnimation(this, R.anim.pulsing_border);
        mapBorder.startAnimation(pulsingAnimation);

        List<String> barbershops = Arrays.asList(
                "1. Kwentong Barbero Balanga",
                "2. Jhun/Dels Barbershop",
                "3. Mando's Barbershop",
                "4. Thrifty haircuts",
                "5. Daddy O.G Barbershop Salon Nail Spa",
                "6. Creatures Barbershop",
                "7. Tonyo's Barbershop",
                "8. Daniel's Jack de Salon",
                "9. Cuts x Kicks by Antonio",
                "10. Pilyo Barbershop",
                "11. Pinuno Elite Barbershop",
                "12. Jovercel Barbershop",
                "13. Black Sparrow Barbershop",
                "14. GWAPO Barbershop and Coffee",
                "15. ADST Barbershop",
                "16. Fel's Barbershop and Hanniielytie II Beaut&Wellness",
                "17. JIM'S Barbershop"
        );

        // --- Coordinates
        Map<String, PointF> shopCoordinates = new HashMap<>();
        shopCoordinates.put("1. Kwentong Barbero Balanga", new PointF(7100, 2240));
        shopCoordinates.put("2. Jhun/Dels Barbershop", new PointF(7500, 3180));
        shopCoordinates.put("3. Mando's Barbershop", new PointF(7600, 3250));
        shopCoordinates.put("4. Thrifty haircuts", new PointF(7750, 5250));
        shopCoordinates.put("5. Daddy O.G Barbershop Salon Nail Spa", new PointF(6750, 2900));
        shopCoordinates.put("6. Creatures Barbershop", new PointF(6910,3280));
        shopCoordinates.put("7. Tonyo's Barbershop", new PointF(6930,3500));
        shopCoordinates.put("8. Daniel's Jack de Salon", new PointF(6550,3420));
        shopCoordinates.put("9. Cuts x Kicks by Antonio", new PointF(6430,3480));
        shopCoordinates.put("10. Pilyo Barbershop", new PointF(6460,3560));
        shopCoordinates.put("11. Pinuno Elite Barbershop", new PointF(6310,3520));
        shopCoordinates.put("12. Jovercel Barbershop", new PointF(6250,3765));
        shopCoordinates.put("13. Black Sparrow Barbershop", new PointF(5350,3420));
        shopCoordinates.put("14. GWAPO Barbershop and Coffee", new PointF(5420,4350));
        shopCoordinates.put("15. ADST Barbershop", new PointF(3735,2650));
        shopCoordinates.put("16. Fel's Barbershop and Hanniielytie II Beaut&Wellness", new PointF(2850,3970));
        shopCoordinates.put("17. JIM'S Barbershop", new PointF(2015,4050));

        // --- Links -->
        Map<String, String> shopLinks = getStringStringMap();

        // --- RecyclerView setup
        RecyclerView recyclerView = findViewById(R.id.barbershop_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        BarbershopAdapter adapter = new BarbershopAdapter(barbershops, name -> {
            // Pan to shop coordinates
            PointF targetPoint = shopCoordinates.get(name);
            if (targetPoint != null) {
                zoomableImageView.panTo(targetPoint);
            }

            // Enable Go To button with link
            currentShopLink = shopLinks.get(name);
            if (currentShopLink != null) {
                goToButton.setEnabled(true);
                goToButton.setAlpha(1f);
            }
        });
        recyclerView.setAdapter(adapter);

        // --- Reset button
        resetButton.setOnClickListener(v -> {
            zoomableImageView.resetView();
            // Disable Go To button
            currentShopLink = null;
            goToButton.setEnabled(false);
            goToButton.setAlpha(0.5f);
        });

        // --- Go To button
        goToButton.setOnClickListener(v -> {
            if (currentShopLink != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentShopLink));
                startActivity(intent);
            }
        });

        // --- Return button
        returnbutton.setOnClickListener(v -> {
            String role = getIntent().getStringExtra("role");
            if ("admin".equals(role)) {
                startActivity(new Intent(Maps.this, HomepageAdmin.class));
            } else {
                startActivity(new Intent(Maps.this, HomepageActivity.class));
            }
            finish();
        });

        // --- Home button
        homebutton.setOnClickListener(v -> {
            String role = getIntent().getStringExtra("role");
            if ("admin".equals(role)) {
                startActivity(new Intent(Maps.this, HomepageAdmin.class));
            } else {
                startActivity(new Intent(Maps.this, HomepageActivity.class));
            }
            finish();
        });
    }

    @NonNull
    private static Map<String, String> getStringStringMap() {
        Map<String, String> shopLinks = new HashMap<>();
        shopLinks.put("1. Kwentong Barbero Balanga", "https://maps.app.goo.gl/zD8HUgL7pxjsZAFW7");
        shopLinks.put("2. Jhun/Dels Barbershop", "https://maps.app.goo.gl/vJJ7UU8oFSS16dGN6");
        shopLinks.put("3. Mando's Barbershop", "https://maps.app.goo.gl/ve7UBi7aPaSakDZE8");
        shopLinks.put("4. Thrifty haircuts", "https://maps.app.goo.gl/yMPtzDhfFqpbPceA7");
        shopLinks.put("5. Daddy O.G Barbershop Salon Nail Spa", "https://maps.app.goo.gl/QHNzAuNPMY5zkh5x6");
        shopLinks.put("6. Creatures Barbershop", "https://maps.app.goo.gl/TRsb7xcpA9WoK1cF7");
        shopLinks.put("7. Tonyo's Barbershop", "https://maps.app.goo.gl/L8kGAuLawy9Kxh6C7");
        shopLinks.put("8. Daniel's Jack de Salon", "https://maps.app.goo.gl/TDe7z2afoUgGPXd99");
        shopLinks.put("9. Cuts x Kicks by Antonio", "https://maps.app.goo.gl/8p5xPkRv2AviEM7Y9");
        shopLinks.put("10. Pilyo Barbershop", "https://maps.app.goo.gl/4emaSuVdNw76JpWU8");
        shopLinks.put("11. Pinuno Elite Barbershop", "https://maps.app.goo.gl/ddyboLBY4Dv1L6jh8");
        shopLinks.put("12. Jovercel Barbershop", "https://maps.app.goo.gl/tZcnGRyKuTRkYSvh9");
        shopLinks.put("13. Black Sparrow Barbershop", "https://maps.app.goo.gl/NQtcQvih6L2fxAE26");
        shopLinks.put("14. GWAPO Barbershop and Coffee", "https://maps.app.goo.gl/b3gafr4JENzneedY6");
        shopLinks.put("15. ADST Barbershop", "https://maps.app.goo.gl/pVcQJ2s39HTsSh1W7");
        shopLinks.put("16. Fel's Barbershop and Hanniielytie II Beaut&Wellness", "https://maps.app.goo.gl/uzsKZELLH62MQ7vLA");
        shopLinks.put("17. JIM'S Barbershop", "https://maps.app.goo.gl/ojQDjShbNwaP7y2x6");
        return shopLinks;
    }
}
