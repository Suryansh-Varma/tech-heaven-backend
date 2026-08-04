package com.ansh.smart_commerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ansh.smart_commerce.TestFixtures;
import com.ansh.smart_commerce.dto.AddressRequest;
import com.ansh.smart_commerce.dto.AddressResponse;
import com.ansh.smart_commerce.entity.Address;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.exception.AddressNotFoundException;
import com.ansh.smart_commerce.repository.AddressRepository;
import com.ansh.smart_commerce.security.SecurityHelper;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private SecurityHelper securityHelper;

    @InjectMocks
    private AddressService addressService;

    @Test
    void addAddress_shouldSaveAddressAndClearPreviousDefault() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Address existingDefault = TestFixtures.address(10L, user, true);
        AddressRequest request = buildRequest(true);

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByUserAndIsDefaultTrue(user)).thenReturn(Optional.of(existingDefault));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address address = invocation.getArgument(0);
            if (ReflectionTestUtils.getField(address, "id") == null) {
                ReflectionTestUtils.setField(address, "id", 20L);
            }
            return address;
        });

        AddressResponse response = addressService.addAddress(user.getId(), request);

        assertEquals(20L, response.getId());
        assertEquals("Alice Home", response.getFullName());
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void getAddressesByUser_shouldMapResponses() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Address address = TestFixtures.address(10L, user, true);

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByUser(user)).thenReturn(List.of(address));

        List<AddressResponse> responses = addressService.getAddressesByUser(user.getId());

        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).getId());
    }

    @Test
    void updateAddress_shouldRejectForeignAddress() {
        User currentUser = TestFixtures.user(1L, "Alice", "alice@example.com");
        User otherUser = TestFixtures.user(2L, "Bob", "bob@example.com");
        Address address = TestFixtures.address(11L, otherUser, false);

        when(securityHelper.getCurrentUser()).thenReturn(currentUser);
        when(addressRepository.findById(11L)).thenReturn(Optional.of(address));

        assertThrows(IllegalArgumentException.class, () -> addressService.updateAddress(11L, buildRequest(false)));
    }

    @Test
    void deleteAddress_shouldRemoveOwnedAddress() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Address address = TestFixtures.address(11L, user, false);

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(addressRepository.findById(11L)).thenReturn(Optional.of(address));

        addressService.deleteAddress(11L);

        verify(addressRepository).delete(address);
    }

    @Test
    void setDefault_shouldClearPreviousDefaultAndPersistNewOne() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");
        Address existingDefault = TestFixtures.address(10L, user, true);
        Address address = TestFixtures.address(11L, user, false);

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(addressRepository.findById(11L)).thenReturn(Optional.of(address));
        when(addressRepository.findByUserAndIsDefaultTrue(user)).thenReturn(Optional.of(existingDefault));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddressResponse response = addressService.setDefault(11L);

        assertEquals(11L, response.getId());
        verify(addressRepository).save(existingDefault);
        verify(addressRepository).save(address);
    }

    @Test
    void updateAddress_shouldThrowWhenAddressMissing() {
        User user = TestFixtures.user(1L, "Alice", "alice@example.com");

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(addressRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class, () -> addressService.updateAddress(99L, buildRequest(false)));
    }

    private AddressRequest buildRequest(boolean isDefault) {
        AddressRequest request = new AddressRequest();
        request.setFullName("Alice Home");
        request.setPhoneNumber("9876543210");
        request.setHouseNumber("12A");
        request.setStreet("Main Street");
        request.setLandmark("Near Park");
        request.setCity("Bengaluru");
        request.setState("Karnataka");
        request.setPostalCode("560001");
        request.setCountry("India");
        request.setDefault(isDefault);
        return request;
    }
}