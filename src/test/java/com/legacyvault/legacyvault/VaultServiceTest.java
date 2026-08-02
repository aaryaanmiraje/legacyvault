package com.legacyvault.legacyvault;

import com.legacyvault.legacyvault.model.User;
import com.legacyvault.legacyvault.model.VaultEntry;
import com.legacyvault.legacyvault.repository.UserRepository;
import com.legacyvault.legacyvault.repository.VaultEntryRepository;
import com.legacyvault.legacyvault.service.EncryptionService;
import com.legacyvault.legacyvault.service.VaultService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VaultServiceTest {

    private VaultEntryRepository vaultEntryRepository;
    private UserRepository userRepository;
    private EncryptionService encryptionService;
    private VaultService vaultService;

    private User user;

    @BeforeEach
    void setUp() {

        vaultEntryRepository = mock(VaultEntryRepository.class);
        userRepository = mock(UserRepository.class);
        encryptionService = mock(EncryptionService.class);

        vaultService = new VaultService(
                vaultEntryRepository,
                userRepository,
                encryptionService
        );

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        user = new User(
                "Test User",
                "test@example.com",
                encoder.encode("AccountPassword123!")
        );
    }


    // TEST 1
    // Password must be encrypted before the entry is saved
    @Test
    void shouldEncryptPasswordWhenCreatingEntry() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(encryptionService.encrypt("Secret123!"))
                .thenReturn("ENCRYPTED_PASSWORD");

        when(vaultEntryRepository.save(any(VaultEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VaultEntry entry = vaultService.createEntry(
                "test@example.com",
                "GitHub",
                "testuser",
                "https://github.com",
                "Secret123!",
                "Test account"
        );

        assertNotNull(entry);

        assertEquals(
                "ENCRYPTED_PASSWORD",
                entry.getEncryptedPassword()
        );

        assertNotEquals(
                "Secret123!",
                entry.getEncryptedPassword()
        );

        verify(encryptionService, times(1))
                .encrypt("Secret123!");

        verify(vaultEntryRepository, times(1))
                .save(any(VaultEntry.class));
    }


    // TEST 2
    // Getting all entries should only query entries belonging to the user
    @Test
    void shouldGetOnlyOwnersEntries() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        VaultEntry entry = new VaultEntry(
                "GitHub",
                "testuser",
                "https://github.com",
                "encrypted",
                "notes",
                user
        );

        when(vaultEntryRepository.findByOwner(user))
                .thenReturn(List.of(entry));

        List<VaultEntry> entries =
                vaultService.getEntries("test@example.com");

        assertEquals(1, entries.size());

        assertEquals(
                "GitHub",
                entries.get(0).getTitle()
        );

        verify(vaultEntryRepository, times(1))
                .findByOwner(user);
    }


    // TEST 3
    // User should be able to retrieve their own entry
    @Test
    void shouldGetOwnedEntry() {

        VaultEntry entry = new VaultEntry(
                "GitHub",
                "testuser",
                "https://github.com",
                "encrypted",
                "notes",
                user
        );

        when(vaultEntryRepository.findByIdAndOwnerEmail(
                1L,
                "test@example.com"
        )).thenReturn(Optional.of(entry));

        VaultEntry result =
                vaultService.getEntryById(
                        1L,
                        "test@example.com"
                );

        assertNotNull(result);

        assertEquals(
                "GitHub",
                result.getTitle()
        );
    }


    // TEST 4
    // A user must NOT be able to access another user's entry
    @Test
    void shouldRejectAccessToAnotherUsersEntry() {

        when(vaultEntryRepository.findByIdAndOwnerEmail(
                1L,
                "attacker@example.com"
        )).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> vaultService.getEntryById(
                                1L,
                                "attacker@example.com"
                        )
                );

        assertEquals(
                404,
                exception.getStatusCode().value()
        );
    }


    // TEST 5
    // Correct account password should reveal vault password
    @Test
    void shouldRevealPasswordWithCorrectAccountPassword() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        VaultEntry entry = new VaultEntry(
                "GitHub",
                "testuser",
                "https://github.com",
                "ENCRYPTED_PASSWORD",
                "notes",
                user
        );

        when(vaultEntryRepository.findByIdAndOwnerEmail(
                1L,
                "test@example.com"
        )).thenReturn(Optional.of(entry));

        when(encryptionService.decrypt("ENCRYPTED_PASSWORD"))
                .thenReturn("VaultPassword123!");

        String password =
                vaultService.revealPassword(
                        1L,
                        "test@example.com",
                        "AccountPassword123!"
                );

        assertEquals(
                "VaultPassword123!",
                password
        );

        verify(encryptionService, times(1))
                .decrypt("ENCRYPTED_PASSWORD");
    }


    // TEST 6
    // Wrong account password must NOT reveal the vault password
    @Test
    void shouldRejectRevealWithWrongAccountPassword() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> vaultService.revealPassword(
                                1L,
                                "test@example.com",
                                "WrongPassword"
                        )
                );

        assertEquals(
                401,
                exception.getStatusCode().value()
        );

        verify(encryptionService, never())
                .decrypt(anyString());

        verify(vaultEntryRepository, never())
                .findByIdAndOwnerEmail(anyLong(), anyString());
    }


    // TEST 7
    // Even with the correct account password,
    // another user's entry must not be revealed
    @Test
    void shouldRejectRevealOfAnotherUsersEntry() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(vaultEntryRepository.findByIdAndOwnerEmail(
                99L,
                "test@example.com"
        )).thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> vaultService.revealPassword(
                                99L,
                                "test@example.com",
                                "AccountPassword123!"
                        )
                );

        assertEquals(
                404,
                exception.getStatusCode().value()
        );

        verify(encryptionService, never())
                .decrypt(anyString());
    }
}