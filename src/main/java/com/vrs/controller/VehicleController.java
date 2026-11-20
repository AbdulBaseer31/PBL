package com.vrs.controller;

import com.vrs.model.Vehicle;
import com.vrs.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    public static class AddVehicleRequest {
        public String numberplate;
        public String type;
        public String brand;
        public BigDecimal basePrice;
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> searchVehicles(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean availableOnly,
            @RequestParam(required = false) String sortBy) {
        List<Vehicle> list = vehicleService.searchVehicles(type, brand, maxPrice, availableOnly, sortBy);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Integer>> getStats() {
        return ResponseEntity.ok(vehicleService.getStats());
    }

    @PostMapping
    public ResponseEntity<Vehicle> addVehicle(@RequestBody AddVehicleRequest req) {
        Vehicle vehicle = vehicleService.addVehicle(req.numberplate, req.type, req.brand, req.basePrice);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }

    @DeleteMapping("/{numberplate}")
    public ResponseEntity<Void> removeVehicle(@PathVariable String numberplate) {
        vehicleService.removeVehicle(numberplate);
        return ResponseEntity.noContent().build();
    }
}

