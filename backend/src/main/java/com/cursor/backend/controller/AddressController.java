package com.cursor.backend.controller;

import com.cursor.backend.dto.AddressRequest;
import com.cursor.backend.dto.AddressResponse;
import com.cursor.backend.dto.ApiResponse;
import com.cursor.backend.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "List user addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(addressService.getUserAddresses(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.ok(addressService.getAddressById(userDetails.getUsername(), id));
    }

    @PostMapping
    @Operation(summary = "Create a new address")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        return ApiResponse.created(addressService.createAddress(userDetails.getUsername(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an address")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {
        return ApiResponse.ok(addressService.updateAddress(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        addressService.deleteAddress(userDetails.getUsername(), id);
        return ApiResponse.ok(null);
    }
}
