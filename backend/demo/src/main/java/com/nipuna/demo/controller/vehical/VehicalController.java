package com.nipuna.demo.controller.vehical;

import com.nipuna.demo.dto.MessageResponse;
import com.nipuna.demo.dto.vehical.VehicleRequestDto;
import com.nipuna.demo.dto.vehical.VehicleResponseDto;
import com.nipuna.demo.security.UserPrincipal;
import com.nipuna.demo.service.vehical.VehicalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// REST Controller for Vehicle Management

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicalController {

    private final VehicalService vehicalService;


     // Add a new vehicle for the authenticated customer
     // POST /api/vehicles

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<VehicleResponseDto> addVehicle(
            @Valid @RequestBody VehicleRequestDto vehicleRequestDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Get the authenticated customer's ID
        Long customerId = userPrincipal.getId();

        // Call service to add the vehicle
        VehicleResponseDto responseDto = vehicalService.addVehicle(vehicleRequestDto, customerId);

        // Return created vehicle with HTTP 201 Created status
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }


    // Get all vehicles for the authenticated customer
    // GET /api/vehicles

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<VehicleResponseDto>> getMyVehicles(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Get the authenticated customer's ID
        Long customerId = userPrincipal.getId();

        // Call service to get all vehicles for this customer
        List<VehicleResponseDto> vehicles = vehicalService.getVehiclesByCustomerId(customerId);

        // Return list of vehicles with HTTP 200 OK status
        return ResponseEntity.ok(vehicles);
    }


     // Get all vehicles in the system (Admin only)
     // GET /api/vehicles/all

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<List<VehicleResponseDto>> getAllVehicles() {

        // Call service to get all vehicles in the system
        List<VehicleResponseDto> vehicles = vehicalService.getAllVehicles();

        // Return list of all vehicles with HTTP 200 OK status
        return ResponseEntity.ok(vehicles);
    }


     // Get a specific vehicle by ID
     // GET /api/vehicles/{id}

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<VehicleResponseDto> getVehicleById(
            @PathVariable("id") Long vehicleId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Call service to get vehicle by ID
        VehicleResponseDto vehicle = vehicalService.getVehicleById(vehicleId);

        // Return vehicle details with HTTP 200 OK status
        return ResponseEntity.ok(vehicle);
    }


     // Update vehicle details
     // PUT /api/vehicles/{id}

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<VehicleResponseDto> updateVehicle(
            @PathVariable("id") Long vehicleId,
            @Valid @RequestBody VehicleRequestDto vehicleRequestDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Get the authenticated customer's ID
        Long customerId = userPrincipal.getId();

        // Call service to update the vehicle
        VehicleResponseDto updatedVehicle = vehicalService.updateVehicle(vehicleId, vehicleRequestDto, customerId);

        // Return updated vehicle with HTTP 200 OK status
        return ResponseEntity.ok(updatedVehicle);
    }


     // Delete a vehicle by ID
     // DELETE /api/vehicles/{id}

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<MessageResponse> deleteVehicle(
            @PathVariable("id") Long vehicleId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Get the authenticated customer's ID
        Long customerId = userPrincipal.getId();

        // Call service to delete the vehicle
        vehicalService.deleteVehicle(vehicleId, customerId);

        // Return success message with HTTP 200 OK status
        return ResponseEntity.ok(new MessageResponse("Vehicle deleted successfully"));
    }


     // Get vehicles by customer ID (Admin/Technician only)
     // GET /api/vehicles/customer/{customerId}

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<List<VehicleResponseDto>> getVehiclesByCustomerId(
            @PathVariable("customerId") Long customerId) {

        // Call service to get all vehicles for the specified customer
        List<VehicleResponseDto> vehicles = vehicalService.getVehiclesByCustomerId(customerId);

        // Return list of vehicles with HTTP 200 OK status
        return ResponseEntity.ok(vehicles);
    }
}


