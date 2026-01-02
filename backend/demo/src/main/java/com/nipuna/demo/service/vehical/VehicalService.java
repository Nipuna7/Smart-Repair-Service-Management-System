package com.nipuna.demo.service.vehical;

import com.nipuna.demo.dto.vehical.VehicleRequestDto;
import com.nipuna.demo.dto.vehical.VehicleResponseDto;
import com.nipuna.demo.entity.User;
import com.nipuna.demo.entity.Vehicle;
import com.nipuna.demo.repository.UserRepository;
import com.nipuna.demo.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class VehicalService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;


    //Add a new vehicle for a specific customer
    @Transactional
    public VehicleResponseDto addVehicle(VehicleRequestDto vehicleRequestDto, Long customerId) {
        // Find the customer by ID
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        // Check if vehicle with the same number already exists for this customer
        if (vehicleRepository.existsByVehicleNumberAndCustomerId(
                vehicleRequestDto.getVehicleNumber(), customerId)) {
            throw new RuntimeException("Vehicle with number " + vehicleRequestDto.getVehicleNumber()
                    + " already exists for this customer");
        }

        // Create new vehicle entity
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber(vehicleRequestDto.getVehicleNumber());
        vehicle.setMake(vehicleRequestDto.getMake());
        vehicle.setModel(vehicleRequestDto.getModel());
        vehicle.setYear(vehicleRequestDto.getYear());
        vehicle.setVehicleType(vehicleRequestDto.getVehicleType());
        vehicle.setCustomer(customer);

        // Save vehicle to database
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // Convert entity to DTO and return
        return mapToResponseDto(savedVehicle);
    }


     // Get all vehicles in the system

    @Transactional(readOnly = true)
    public List<VehicleResponseDto> getAllVehicles() {
        // Fetch all vehicles and convert to DTOs
        return vehicleRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }


    // Get all vehicles for a specific customer
    @Transactional(readOnly = true)
    public List<VehicleResponseDto> getVehiclesByCustomerId(Long customerId) {
        // Fetch vehicles by customer ID and convert to DTOs
        return vehicleRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }


     // Get a specific vehicle by its Id
    @Transactional(readOnly = true)
    public VehicleResponseDto getVehicleById(Long vehicleId) {
        // Find vehicle by ID
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        // Convert to DTO and return
        return mapToResponseDto(vehicle);
    }


    // Update vehicle details
    @Transactional
    public VehicleResponseDto updateVehicle(Long vehicleId, VehicleRequestDto vehicleRequestDto, Long customerId) {
        // Find the vehicle by ID
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        // Check if the vehicle belongs to the customer (authorization)
        if (!vehicle.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("You are not authorized to update this vehicle");
        }

        // Check if the new vehicle number conflicts with another vehicle
        if (!vehicle.getVehicleNumber().equals(vehicleRequestDto.getVehicleNumber()) &&
                vehicleRepository.existsByVehicleNumberAndCustomerId(
                        vehicleRequestDto.getVehicleNumber(), customerId)) {
            throw new RuntimeException("Vehicle with number " + vehicleRequestDto.getVehicleNumber()
                    + " already exists for this customer");
        }

        // Update vehicle fields
        vehicle.setVehicleNumber(vehicleRequestDto.getVehicleNumber());
        vehicle.setMake(vehicleRequestDto.getMake());
        vehicle.setModel(vehicleRequestDto.getModel());
        vehicle.setYear(vehicleRequestDto.getYear());
        vehicle.setVehicleType(vehicleRequestDto.getVehicleType());

        // Save updated vehicle
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);

        // Convert to DTO and return
        return mapToResponseDto(updatedVehicle);
    }


    // Delete a vehicle by its ID
    @Transactional
    public void deleteVehicle(Long vehicleId, Long customerId) {
        // Find the vehicle by ID
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));

        // Check if the vehicle belongs to the customer (authorization)
        if (!vehicle.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("You are not authorized to delete this vehicle");
        }

        // Delete the vehicle from database
        vehicleRepository.delete(vehicle);
    }

    
    // Helper method to convert Vehicle entity to VehicleResponseDto

    private VehicleResponseDto mapToResponseDto(Vehicle vehicle) {
        return VehicleResponseDto.builder()
                .id(vehicle.getId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .vehicleType(vehicle.getVehicleType())
                .customerId(vehicle.getCustomer().getId())
                .customerName(vehicle.getCustomer().getFullName())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}
