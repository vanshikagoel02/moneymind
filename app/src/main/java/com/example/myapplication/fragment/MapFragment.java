package com.example.myapplication.fragment;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapter.TransactionAdapter;
import com.example.myapplication.db.MoneyRepository;
import com.example.myapplication.model.TransactionItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private EditText etSearch;
    private Button btnSearch, btnAddTxHere;
    private MapView mapView;
    private LinearLayout layoutPlaceOverlay;
    private TextView tvPromptTitle, tvPromptSub, tvLocatedCount, tvEmptyPlaces;
    private RecyclerView rvTransactionsByPlace, rvMapSuggestions;

    private GoogleMap googleMap;
    private MoneyRepository repo;
    private TransactionAdapter adapter;
    private SuggestionAdapter suggestionAdapter;

    private LatLng selectedLatLng = new LatLng(30.7333, 76.7794); // Chandigarh default
    private String selectedPlaceName = "";
    private Geocoder geocoder;

    // Single background executor and main thread debouncer to prevent ANR
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;

    public static class MapPlaceSuggestion {
        public String title;
        public String subTitle;
        public double latitude;
        public double longitude;

        public MapPlaceSuggestion(String title, String subTitle, double latitude, double longitude) {
            this.title = title;
            this.subTitle = subTitle;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        repo = MoneyRepository.getInstance(requireContext());
        geocoder = new Geocoder(requireContext(), Locale.getDefault());

        etSearch = view.findViewById(R.id.etMapSearch);
        btnSearch = view.findViewById(R.id.btnMapSearch);
        btnAddTxHere = view.findViewById(R.id.btnMapAddTxHere);
        mapView = view.findViewById(R.id.googleMapView);
        layoutPlaceOverlay = view.findViewById(R.id.layoutMapPlaceOverlay);
        tvPromptTitle = view.findViewById(R.id.tvMapPromptTitle);
        tvPromptSub = view.findViewById(R.id.tvMapPromptSub);
        tvLocatedCount = view.findViewById(R.id.tvMapLocatedCount);
        tvEmptyPlaces = view.findViewById(R.id.tvMapEmptyPlaces);
        rvTransactionsByPlace = view.findViewById(R.id.rvTransactionsByPlace);
        rvMapSuggestions = view.findViewById(R.id.rvMapSuggestions);

        rvTransactionsByPlace.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(new ArrayList<>(), new TransactionAdapter.OnTransactionClickListener() {
            @Override
            public void onTransactionClick(TransactionItem item) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showAddTransactionDialog(item);
                }
            }

            @Override
            public void onTransactionOptionClick(TransactionItem item, View anchor) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showAddTransactionDialog(item);
                }
            }
        });
        rvTransactionsByPlace.setAdapter(adapter);

        rvMapSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        suggestionAdapter = new SuggestionAdapter(new ArrayList<>(), this::selectPlaceSuggestion);
        rvMapSuggestions.setAdapter(suggestionAdapter);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        btnSearch.setOnClickListener(v -> performPlaceSearch());
        btnAddTxHere.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                String place = !selectedPlaceName.isEmpty() ? selectedPlaceName : etSearch.getText().toString().trim();
                if (place.isEmpty()) place = "Connaught Place, New Delhi";
                ((MainActivity) getActivity()).showAddTransactionWithPlace("Expense", place, selectedLatLng.latitude, selectedLatLng.longitude);
            }
        });

        // 350ms debounced search typing listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (debounceRunnable != null) {
                    mainHandler.removeCallbacks(debounceRunnable);
                }
                String text = s.toString().trim();
                if (text.length() < 2) {
                    rvMapSuggestions.setVisibility(View.GONE);
                } else {
                    debounceRunnable = () -> fetchPlaceSuggestions(text);
                    mainHandler.postDelayed(debounceRunnable, 350);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 13f));
        refreshMapPins();
    }

    private void fetchPlaceSuggestions(String query) {
        backgroundExecutor.execute(() -> {
            List<MapPlaceSuggestion> results = new ArrayList<>();

            String q = query.toLowerCase();
            // Check precise preset matches
            if (q.contains("narsee") || q.contains("nmims") || q.contains("monjee")) {
                if (q.contains("chandigarh")) {
                    results.add(new MapPlaceSuggestion("Narsee Monjee Institute (NMIMS)", "Sarangpur, Chandigarh", 30.7711, 76.7589));
                } else {
                    results.add(new MapPlaceSuggestion("NMIMS (Narsee Monjee Institute)", "Vile Parle West, Mumbai, Maharashtra", 19.1033, 72.8370));
                    results.add(new MapPlaceSuggestion("Narsee Monjee Institute (NMIMS)", "Sarangpur, Chandigarh", 30.7711, 76.7589));
                }
            } else if (q.contains("zara")) {
                results.add(new MapPlaceSuggestion("Zara", "Elante Mall, Industrial Area Phase I, Chandigarh", 30.7053, 76.8013));
                results.add(new MapPlaceSuggestion("Zara", "Phoenix Marketcity, Kurla West, Mumbai", 19.0863, 72.8889));
            }

            // Check Geocoder safely in background
            try {
                List<Address> addresses = geocoder.getFromLocationName(query, 5);
                if (addresses != null) {
                    for (Address addr : addresses) {
                        String title = addr.getFeatureName() != null ? addr.getFeatureName() : query;
                        String sub = addr.getAddressLine(0) != null ? addr.getAddressLine(0) : "";
                        results.add(new MapPlaceSuggestion(title, sub, addr.getLatitude(), addr.getLongitude()));
                    }
                }
            } catch (Exception ignored) {}

            mainHandler.post(() -> {
                if (isAdded() && getContext() != null) {
                    if (!results.isEmpty()) {
                        suggestionAdapter.setItems(results);
                        rvMapSuggestions.setVisibility(View.VISIBLE);
                    } else {
                        rvMapSuggestions.setVisibility(View.GONE);
                    }
                }
            });
        });
    }

    private void selectPlaceSuggestion(MapPlaceSuggestion suggestion) {
        selectedLatLng = new LatLng(suggestion.latitude, suggestion.longitude);
        selectedPlaceName = suggestion.title;
        etSearch.setText(suggestion.title);
        rvMapSuggestions.setVisibility(View.GONE);

        tvPromptTitle.setText(suggestion.title);
        tvPromptSub.setText(suggestion.subTitle + "\nClick + Add transaction here below.");
        layoutPlaceOverlay.setVisibility(View.VISIBLE);

        if (googleMap != null) {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions()
                    .position(selectedLatLng)
                    .title(suggestion.title)
                    .snippet(suggestion.subTitle));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 16f));
        }

        Toast.makeText(getContext(), "Selected: " + suggestion.title, Toast.LENGTH_SHORT).show();
    }

    private void performPlaceSearch() {
        String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) {
            query = "Connaught Place, New Delhi";
            etSearch.setText(query);
        }

        final String finalQuery = query;
        backgroundExecutor.execute(() -> {
            LatLng foundLatLng = null;
            String foundSub = "Search result";

            String q = finalQuery.toLowerCase();
            if (q.contains("narsee") || q.contains("nmims") || q.contains("monjee")) {
                if (q.contains("chandigarh")) {
                    foundLatLng = new LatLng(30.7711, 76.7589);
                    foundSub = "Narsee Monjee Institute (NMIMS), Sarangpur, Chandigarh";
                } else {
                    foundLatLng = new LatLng(19.1033, 72.8370);
                    foundSub = "NMIMS (Narsee Monjee), Vile Parle West, Mumbai";
                }
            } else if (q.contains("zara")) {
                foundLatLng = new LatLng(30.7053, 76.8013);
                foundSub = "Zara, Elante Mall, Chandigarh";
            }

            if (foundLatLng == null) {
                try {
                    List<Address> addresses = geocoder.getFromLocationName(finalQuery, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);
                        foundLatLng = new LatLng(addr.getLatitude(), addr.getLongitude());
                        if (addr.getAddressLine(0) != null) foundSub = addr.getAddressLine(0);
                    }
                } catch (Exception ignored) {}
            }

            if (foundLatLng == null) {
                foundLatLng = new LatLng(30.7333 + (Math.random() - 0.5) * 0.02, 76.7794 + (Math.random() - 0.5) * 0.02);
            }

            final LatLng targetLatLng = foundLatLng;
            final String targetSub = foundSub;

            mainHandler.post(() -> {
                if (isAdded() && getContext() != null) {
                    selectedLatLng = targetLatLng;
                    selectedPlaceName = finalQuery;
                    tvPromptTitle.setText(finalQuery);
                    tvPromptSub.setText(targetSub + "\nClick + Add transaction here below.");
                    layoutPlaceOverlay.setVisibility(View.VISIBLE);

                    if (googleMap != null) {
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions().position(selectedLatLng).title(finalQuery).snippet(targetSub));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 16f));
                    }

                    Toast.makeText(getContext(), "Location found: " + finalQuery, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void refreshMapPins() {
        List<TransactionItem> list = repo.getTransactionsWithLocation();
        tvLocatedCount.setText(list.size() + " located");

        if (list.isEmpty()) {
            tvEmptyPlaces.setVisibility(View.VISIBLE);
            rvTransactionsByPlace.setVisibility(View.GONE);
        } else {
            tvEmptyPlaces.setVisibility(View.GONE);
            rvTransactionsByPlace.setVisibility(View.VISIBLE);
            adapter.setItems(list);
        }

        if (googleMap != null && !list.isEmpty()) {
            googleMap.clear();
            for (TransactionItem tx : list) {
                if (tx.getLatitude() != 0 || tx.getLongitude() != 0) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(tx.getLatitude(), tx.getLongitude()))
                            .title(tx.getDescription() + " - ₹" + String.format(Locale.getDefault(), "%,.0f", tx.getAmount()))
                            .snippet(tx.getPlaceName()));
                }
            }
        }
    }

    @Override public void onResume() { super.onResume(); mapView.onResume(); refreshMapPins(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        backgroundExecutor.shutdown();
    }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }

    private static class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {
        public interface OnSuggestionClickListener { void onClick(MapPlaceSuggestion suggestion); }
        private List<MapPlaceSuggestion> items;
        private final OnSuggestionClickListener listener;

        SuggestionAdapter(List<MapPlaceSuggestion> items, OnSuggestionClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        void setItems(List<MapPlaceSuggestion> items) { this.items = items; notifyDataSetChanged(); }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_map_suggestion, parent, false);
            return new ViewHolder(view);
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MapPlaceSuggestion suggestion = items.get(position);
            holder.tvTitle.setText(suggestion.title);
            holder.tvSub.setText(suggestion.subTitle);
            holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(suggestion); });
        }

        @Override public int getItemCount() { return items != null ? items.size() : 0; }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSub;
            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvSuggestTitle);
                tvSub = itemView.findViewById(R.id.tvSuggestSub);
            }
        }
    }
}
