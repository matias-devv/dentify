package com.dentify.domain.medicalhistory.service;

import static org.junit.jupiter.api.Assertions.*;

import com.dentify.domain.allergycatalog.model.AllergyCatalog;
import com.dentify.domain.allergycatalog.service.IAllergyCatalogService;
import com.dentify.domain.clinic.model.Clinic;
import com.dentify.domain.dentist.model.Dentist;
import com.dentify.domain.dentist.service.IDentistService;
import com.dentify.domain.medicalhistory.dto.request.CreateMedicalHistoryRequest;
import com.dentify.domain.medicalhistory.dto.response.CreateMedicalHistoryResponse;
import com.dentify.domain.medicalhistory.dto.response.MedicalHistorySummaryResponse;
import com.dentify.domain.medicalhistory.model.MedicalHistory;
import com.dentify.domain.medicalhistory.repository.IMedicalHistoryRepository;
import com.dentify.domain.patient.model.Patient;
import com.dentify.domain.patient.service.IPatientService;
import com.dentify.domain.patientallergy.model.PatientAllergy;
import com.dentify.domain.toothrecord.enums.OdontogramType;
import com.dentify.domain.userProfile.model.UserProfile;
import com.dentify.exception.allergycatalog.AllergiesCatalogNotFoundException;
import com.dentify.exception.dentist.DentistNotFoundException;
import com.dentify.exception.patient.PatientNotFoundException;
import com.dentify.mapper.MedicalHistoryMapper;
import com.dentify.mapper.PatientAllergyMapper;
import com.dentify.security.multitenancy.TenantContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import com.dentify.domain.medicalhistory.dto.response.MedicalHistoryDetailResponse;
import com.dentify.domain.patientallergy.dto.response.PatientAllergyDetailResponse;
import com.dentify.exception.medicalhistory.MedicalHistoryNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalHistoryServiceTest {

    @InjectMocks
    private MedicalHistoryService medicalHistoryService;

    @Mock
    private IMedicalHistoryRepository medicalHistoryRepository;

    @Mock
    private IDentistService dentistService;

    @Mock
    private IPatientService patientService;

    @Mock
    private IAllergyCatalogService allergyCatalogService;

    @Mock
    private MedicalHistoryMapper mapper;

    @Mock
    private PatientAllergyMapper patientAllergyMapper;

    // ── Fixtures reutilizables ─────────────────────────────────────────────────

    private static final String USERNAME    = "dr.perez";
    private static final Long   PATIENT_ID  = 7L;
    private static final Long   DENTIST_ID  = 3L;
    private static final Long   CLINIC_ID   = 1L;

    private Clinic       clinic;
    private UserProfile  userProfile;
    private Dentist      dentist;
    private Patient      patient;

    @BeforeEach
    void setUp() {
        clinic = Clinic.builder()
                        .id(CLINIC_ID)
                        .build();

        userProfile = UserProfile.builder()
                                .id(10L)
                                .name("Juan")
                                .surname("Pérez")
                                .build();

        dentist = Dentist.builder()
                        .id(DENTIST_ID)
                        .active(true)
                        .clinic(clinic)
                        .userProfile(userProfile)
                        .build();

        patient = new Patient();
        patient.setId_patient(PATIENT_ID);
        patient.setName("Martín");
        patient.setSurname("García");
    }

    // ── Helper factories ───────────────────────────────────────────────────────

    private CreateMedicalHistoryRequest baseRequest() {
        CreateMedicalHistoryRequest req = new CreateMedicalHistoryRequest();
        req.setOdontogramType(OdontogramType.ADULT);
        req.setStartDate(LocalDate.of(2025, 6, 1));
        req.setHasAllergies(false);
        req.setAllergyIds(List.of());
        return req;
    }

    private MedicalHistory baseMedicalHistory() {
        MedicalHistory mh = new MedicalHistory();
        mh.setId(12L);
        mh.setStartDate(LocalDate.of(2025, 6, 1));
        mh.setOdontogramType(OdontogramType.ADULT);
        mh.setHasAllergies(false);
        mh.setDentist(dentist);
        mh.setPatient(patient);
        mh.setAllergies(new ArrayList<>());
        return mh;
    }

    private CreateMedicalHistoryResponse stubResponse(MedicalHistory mh) {
        return new CreateMedicalHistoryResponse(
                mh.getId(),
                mh.getStartDate(),
                OdontogramType.ADULT.name(),
                null, null,
                mh.getHasAllergies(),
                null,
                List.of(),
                DENTIST_ID, "Juan", "Pérez",
                PATIENT_ID, "Martín", "García",
                null
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════
    @Nested
    class CreateMedicalHistoryTests {

        // ── Happy path ─────────────────────────────────────────────────────────

        @Test
        void shouldCreateMedicalHistorySuccessfullyWithoutAllergies() {

            // given
            CreateMedicalHistoryRequest request = baseRequest();
            MedicalHistory builtHistory         = baseMedicalHistory();
            CreateMedicalHistoryResponse expectedResponse = stubResponse(builtHistory);

            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenReturn(dentist);
            when(patientService.findPatientByIdAndClinicId(PATIENT_ID, CLINIC_ID))
                    .thenReturn(patient);
            when(mapper.buildMedicalHistory(dentist, patient, request))
                    .thenReturn(builtHistory);
            when(mapper.buildCreateMedicalHistoryResponse(builtHistory))
                    .thenReturn(expectedResponse);

            // when
            CreateMedicalHistoryResponse actualResponse = medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then
            assertNotNull(actualResponse);
            assertEquals(12L,       actualResponse.idMedicalHistory());
            assertEquals(DENTIST_ID, actualResponse.dentistId());
            assertEquals(PATIENT_ID, actualResponse.patientId());

            verify(medicalHistoryRepository, times(1)).save(builtHistory);
            verifyNoInteractions(allergyCatalogService, patientAllergyMapper);
        }

        @Test
        void shouldCreateMedicalHistorySuccessfullyWithAllergies() {

            // given
            List<Long> allergyIds = List.of(1L, 4L);

            CreateMedicalHistoryRequest request = baseRequest();
            request.setHasAllergies(true);
            request.setAllergyIds(allergyIds);

            MedicalHistory builtHistory = baseMedicalHistory();
            builtHistory.setHasAllergies(true);

            AllergyCatalog penicillin = AllergyCatalog.builder().id(1L).name("Penicilina").active(true).build();
            AllergyCatalog latex      = AllergyCatalog.builder().id(4L).name("Látex").active(true).build();
            List<AllergyCatalog> catalogAllergies = List.of(penicillin, latex);

            PatientAllergy pa1 = PatientAllergy.builder().allergy(penicillin).medicalHistory(builtHistory).build();
            PatientAllergy pa2 = PatientAllergy.builder().allergy(latex).medicalHistory(builtHistory).build();
            List<PatientAllergy> patientAllergies = List.of(pa1, pa2);

            CreateMedicalHistoryResponse expectedResponse = stubResponse(builtHistory);

            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenReturn(dentist);
            when(patientService.findPatientByIdAndClinicId(PATIENT_ID, CLINIC_ID))
                    .thenReturn(patient);
            when(mapper.buildMedicalHistory(dentist, patient, request))
                    .thenReturn(builtHistory);
            when(allergyCatalogService.findAllergiesWithThisIds(allergyIds))
                    .thenReturn(catalogAllergies);
            when(patientAllergyMapper.buildPatientAllergyList(catalogAllergies, builtHistory))
                    .thenReturn(patientAllergies);
            when(mapper.buildCreateMedicalHistoryResponse(builtHistory))
                    .thenReturn(expectedResponse);

            // when
            CreateMedicalHistoryResponse actualResponse =
                    medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then
            assertNotNull(actualResponse);

            ArgumentCaptor<MedicalHistory> captor = ArgumentCaptor.forClass(MedicalHistory.class);
            verify(medicalHistoryRepository, times(1)).save(captor.capture());
            MedicalHistory saved = captor.getValue();

            assertTrue(saved.getHasAllergies());
            assertEquals(2, saved.getAllergies().size());

            verify(allergyCatalogService, times(1)).findAllergiesWithThisIds(allergyIds);
            verify(patientAllergyMapper, times(1)).buildPatientAllergyList(catalogAllergies, builtHistory);
        }

        @Test
        void shouldIgnoreAllergyIdsWhenHasAllergiesIsFalse() {

            // given — hasAllergies = false pero llegan allergyIds con valores
            CreateMedicalHistoryRequest request = baseRequest();
            request.setHasAllergies(false);
            request.setAllergyIds(List.of(1L, 2L, 3L)); // deben ser ignorados

            MedicalHistory builtHistory = baseMedicalHistory();
            CreateMedicalHistoryResponse expectedResponse = stubResponse(builtHistory);

            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenReturn(dentist);
            when(patientService.findPatientByIdAndClinicId(PATIENT_ID, CLINIC_ID))
                    .thenReturn(patient);
            when(mapper.buildMedicalHistory(dentist, patient, request))
                    .thenReturn(builtHistory);
            when(mapper.buildCreateMedicalHistoryResponse(builtHistory))
                    .thenReturn(expectedResponse);

            // when
            medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then — jamás debe consultar allergyCatalogService
            verifyNoInteractions(allergyCatalogService, patientAllergyMapper);
            verify(medicalHistoryRepository, times(1)).save(builtHistory);
        }

        @Test
        void shouldCreateMedicalHistoryWhenHasAllergiesIsTrueButAllergyIdsIsEmpty() {

            // given
            CreateMedicalHistoryRequest request = baseRequest();
            request.setHasAllergies(true);
            request.setAllergyIds(List.of()); // vacío: la condición del if no se cumple

            MedicalHistory builtHistory = baseMedicalHistory();
            builtHistory.setHasAllergies(true);
            CreateMedicalHistoryResponse expectedResponse = stubResponse(builtHistory);

            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenReturn(dentist);
            when(patientService.findPatientByIdAndClinicId(PATIENT_ID, CLINIC_ID))
                    .thenReturn(patient);
            when(mapper.buildMedicalHistory(dentist, patient, request))
                    .thenReturn(builtHistory);
            when(mapper.buildCreateMedicalHistoryResponse(builtHistory))
                    .thenReturn(expectedResponse);

            // when
            CreateMedicalHistoryResponse actualResponse =
                    medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then
            assertNotNull(actualResponse);
            verify(medicalHistoryRepository, times(1)).save(builtHistory);
            verifyNoInteractions(allergyCatalogService, patientAllergyMapper);
        }

        @Test
        void shouldCreateMedicalHistoryWhenHasAllergiesIsTrueButAllergyIdsIsNull() {

            // given — allergyIds null (defensivo: null no cumple la condición != null del if)
            CreateMedicalHistoryRequest request = baseRequest();
            request.setHasAllergies(true);
            request.setAllergyIds(null);

            MedicalHistory builtHistory = baseMedicalHistory();
            builtHistory.setHasAllergies(true);
            CreateMedicalHistoryResponse expectedResponse = stubResponse(builtHistory);

            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenReturn(dentist);
            when(patientService.findPatientByIdAndClinicId(PATIENT_ID, CLINIC_ID))
                    .thenReturn(patient);
            when(mapper.buildMedicalHistory(dentist, patient, request))
                    .thenReturn(builtHistory);
            when(mapper.buildCreateMedicalHistoryResponse(builtHistory))
                    .thenReturn(expectedResponse);

            // when
            CreateMedicalHistoryResponse actualResponse =
                    medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then
            assertNotNull(actualResponse);
            verify(medicalHistoryRepository, times(1)).save(builtHistory);
            verifyNoInteractions(allergyCatalogService, patientAllergyMapper);
        }

        // ── Excepciones ────────────────────────────────────────────────────────

        @Test
        void shouldThrowDentistNotFoundExceptionWhenDentistDoesNotExist() {

            // given
            CreateMedicalHistoryRequest request = baseRequest();

            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenThrow(new DentistNotFoundException("The dentist with this username was not found"));

            // when
            Executable executable =
                    () -> medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then
            assertThrows(DentistNotFoundException.class, executable);
            verifyNoInteractions(patientService, medicalHistoryRepository, allergyCatalogService);
        }

        @Test
        void shouldThrowPatientNotFoundExceptionWhenPatientDoesNotExist() {

            // given
            CreateMedicalHistoryRequest request = baseRequest();

            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenReturn(dentist);
            when(patientService.findPatientByIdAndClinicId(PATIENT_ID, CLINIC_ID))
                    .thenThrow(new PatientNotFoundException("The patient with this id: " + PATIENT_ID + " was not found"));

            // when
            Executable executable =
                    () -> medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then
            assertThrows(PatientNotFoundException.class, executable);
            verifyNoInteractions(medicalHistoryRepository, allergyCatalogService);
        }

        @Test
        void shouldThrowPatientNotFoundExceptionWhenPatientBelongsToAnotherTenant() {

            // given — clínica diferente → findPatientByIdAndClinicId no encuentra nada
            CreateMedicalHistoryRequest request = baseRequest();
            Long otherClinicId = 99L;
            Clinic otherClinic = Clinic.builder().id(otherClinicId).build();
            dentist.setClinic(otherClinic);

            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenReturn(dentist);
            when(patientService.findPatientByIdAndClinicId(PATIENT_ID, otherClinicId))
                    .thenThrow(new PatientNotFoundException("The patient with this id: " + PATIENT_ID + " was not found"));

            // when
            Executable executable =
                    () -> medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then — siempre 404, nunca exponer cross-tenant
            PatientNotFoundException exception = assertThrows(PatientNotFoundException.class, executable);
            assertEquals("PATIENT_NOT_FOUND", exception.getErrorCode());
            verifyNoInteractions(medicalHistoryRepository, allergyCatalogService);
        }

        @Test
        void shouldThrowAllergiesCatalogNotFoundExceptionWhenNoneOfTheAllergyIdsExist() {

            // given — el repositorio no encuentra ningún ID (Optional vacío → servicio lanza excepción)
            List<Long> allergyIds = List.of(999L, 1000L);

            CreateMedicalHistoryRequest request = baseRequest();
            request.setHasAllergies(true);
            request.setAllergyIds(allergyIds);

            MedicalHistory builtHistory = baseMedicalHistory();

            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenReturn(dentist);
            when(patientService.findPatientByIdAndClinicId(PATIENT_ID, CLINIC_ID))
                    .thenReturn(patient);
            when(mapper.buildMedicalHistory(dentist, patient, request))
                    .thenReturn(builtHistory);
            when(allergyCatalogService.findAllergiesWithThisIds(allergyIds))
                    .thenThrow(new AllergiesCatalogNotFoundException("There is no allergy record for these ids"));

            // when
            Executable executable =
                    () -> medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then
            assertThrows(AllergiesCatalogNotFoundException.class, executable);
            verify(medicalHistoryRepository, never()).save(any());
        }

        @Test
        void shouldThrowAllergiesCatalogNotFoundExceptionWhenSomeAllergyIdsAreNotFound() {

            // given — se piden 3 IDs pero el catálogo sólo devuelve 2 (uno no existe o está inactivo)
            List<Long> allergyIds = List.of(1L, 4L, 999L);

            CreateMedicalHistoryRequest request = baseRequest();
            request.setHasAllergies(true);
            request.setAllergyIds(allergyIds);

            MedicalHistory builtHistory = baseMedicalHistory();

            AllergyCatalog penicillin = AllergyCatalog.builder().id(1L).name("Penicilina").active(true).build();
            AllergyCatalog latex      = AllergyCatalog.builder().id(4L).name("Látex").active(true).build();
            // ID 999 no existe → foundIds = {1, 4} → missingIds = [999] → lanza excepción

            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenReturn(dentist);
            when(patientService.findPatientByIdAndClinicId(PATIENT_ID, CLINIC_ID))
                    .thenReturn(patient);
            when(mapper.buildMedicalHistory(dentist, patient, request))
                    .thenReturn(builtHistory);
            when(allergyCatalogService.findAllergiesWithThisIds(allergyIds))
                    .thenReturn(List.of(penicillin, latex));

            // when
            Executable executable =
                    () -> medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then
            AllergiesCatalogNotFoundException exception =
                    assertThrows(AllergiesCatalogNotFoundException.class, executable);

            assertTrue(exception.getMessage().contains("999"));
            verify(medicalHistoryRepository, never()).save(any());
        }

        @Test
        void shouldThrowAllergiesCatalogNotFoundExceptionWhenAllergyIsInactive() {

            // given — el allergy_catalog filtra WHERE active = true → el ID inactivo no se devuelve
            List<Long> allergyIds = List.of(5L); // ID 5 existe pero active = false → no lo devuelve la query

            CreateMedicalHistoryRequest request = baseRequest();
            request.setHasAllergies(true);
            request.setAllergyIds(allergyIds);

            MedicalHistory builtHistory = baseMedicalHistory();

            // El repositorio filtra active=true, por lo que devuelve lista vacía → servicio lanza excepción
            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenReturn(dentist);
            when(patientService.findPatientByIdAndClinicId(PATIENT_ID, CLINIC_ID))
                    .thenReturn(patient);
            when(mapper.buildMedicalHistory(dentist, patient, request))
                    .thenReturn(builtHistory);
            when(allergyCatalogService.findAllergiesWithThisIds(allergyIds))
                    .thenThrow(new AllergiesCatalogNotFoundException("There is no allergy record for these ids"));

            // when
            Executable executable =
                    () -> medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then
            assertThrows(AllergiesCatalogNotFoundException.class, executable);
            verify(medicalHistoryRepository, never()).save(any());
        }

        // ── Verificación de flujo ──────────────────────────────────────────────

        @Test
        void shouldResolvePatientUsingClinicIdFromDentist() {

            // given — validamos que se usa dentist.getClinic().getId() y no otro valor
            CreateMedicalHistoryRequest request = baseRequest();
            MedicalHistory builtHistory         = baseMedicalHistory();

            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenReturn(dentist);
            when(patientService.findPatientByIdAndClinicId(PATIENT_ID, CLINIC_ID))
                    .thenReturn(patient);
            when(mapper.buildMedicalHistory(dentist, patient, request))
                    .thenReturn(builtHistory);
            when(mapper.buildCreateMedicalHistoryResponse(builtHistory))
                    .thenReturn(stubResponse(builtHistory));

            // when
            medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then
            verify(patientService, times(1))
                    .findPatientByIdAndClinicId(PATIENT_ID, CLINIC_ID);
        }

        @Test
        void shouldCallSaveExactlyOnceRegardlessOfAllergiesPresence() {

            // given
            CreateMedicalHistoryRequest request = baseRequest();
            request.setHasAllergies(true);
            request.setAllergyIds(List.of(1L));

            MedicalHistory builtHistory = baseMedicalHistory();
            builtHistory.setHasAllergies(true);

            AllergyCatalog penicillin = AllergyCatalog.builder().id(1L).name("Penicilina").active(true).build();
            PatientAllergy pa = PatientAllergy.builder().allergy(penicillin).medicalHistory(builtHistory).build();

            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenReturn(dentist);
            when(patientService.findPatientByIdAndClinicId(PATIENT_ID, CLINIC_ID))
                    .thenReturn(patient);
            when(mapper.buildMedicalHistory(dentist, patient, request))
                    .thenReturn(builtHistory);
            when(allergyCatalogService.findAllergiesWithThisIds(List.of(1L)))
                    .thenReturn(List.of(penicillin));
            when(patientAllergyMapper.buildPatientAllergyList(List.of(penicillin), builtHistory))
                    .thenReturn(List.of(pa));
            when(mapper.buildCreateMedicalHistoryResponse(builtHistory))
                    .thenReturn(stubResponse(builtHistory));

            // when
            medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then
            verify(medicalHistoryRepository, times(1)).save(any(MedicalHistory.class));
            verifyNoMoreInteractions(medicalHistoryRepository);
        }

        @Test
        void shouldPassBuiltMedicalHistoryToMapperForResponse() {

            // given
            CreateMedicalHistoryRequest request = baseRequest();
            MedicalHistory builtHistory         = baseMedicalHistory();
            CreateMedicalHistoryResponse expectedResponse = stubResponse(builtHistory);

            when(dentistService.findDentistByAuthUserUsername(USERNAME))
                    .thenReturn(dentist);
            when(patientService.findPatientByIdAndClinicId(PATIENT_ID, CLINIC_ID))
                    .thenReturn(patient);
            when(mapper.buildMedicalHistory(dentist, patient, request))
                    .thenReturn(builtHistory);
            when(mapper.buildCreateMedicalHistoryResponse(builtHistory))
                    .thenReturn(expectedResponse);

            // when
            CreateMedicalHistoryResponse actualResponse =
                    medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

            // then — el mapper recibe la misma instancia que se persistió
            verify(mapper, times(1)).buildCreateMedicalHistoryResponse(builtHistory);
            assertSame(expectedResponse, actualResponse);
        }
    }

    @Nested
    class FindAllMedicalHistoryTests {

        private static final String TENANT_ID       = "tenant-abc-123";
        private static final String OTHER_TENANT_ID = "tenant-xyz-999";

        private MedicalHistory historyOne;
        private MedicalHistory historyTwo;

        private MedicalHistorySummaryResponse summaryOne;
        private MedicalHistorySummaryResponse summaryTwo;

        @BeforeEach
        void setUpTenantAndHistories() {
            TenantContext.set(TENANT_ID);

            historyOne = buildMedicalHistory(10L, LocalDate.of(2025, 5, 20), null);
            historyTwo = buildMedicalHistory(11L, LocalDate.of(2024, 11, 10), userProfile);

            summaryOne = buildSummary(10L, LocalDate.of(2025, 5, 20));
            summaryTwo = buildSummary(11L, LocalDate.of(2024, 11, 10));
        }

        @AfterEach
        void clearTenantContext() {
            TenantContext.clear();
        }

        // ── Happy path ───────────────────────────────────────────────────────────

        @Test
        @DisplayName("Should return all medical histories mapped correctly when patient has multiple records")
        void shouldReturnAllMedicalHistoriesMappedCorrectly() {

            // given
            List<MedicalHistory> repositoryResult = List.of(historyOne, historyTwo);

            when( patientService.getTenantIdOrThrow(PATIENT_ID) )
                    .thenReturn(TENANT_ID);

            when( medicalHistoryRepository.findAllByPatientIdOrderByStartDateDesc(PATIENT_ID) )
                      .thenReturn(repositoryResult);

            stubCountsFor(historyOne, 5, 2, 1);
            stubCountsFor(historyTwo, 3, 0, 2);

            when( mapper.toSummaryResponse(historyOne, 5, 2, 1) ).thenReturn(summaryOne);
            when( mapper.toSummaryResponse(historyTwo, 3, 0, 2) ).thenReturn(summaryTwo);

            // when
            List<MedicalHistorySummaryResponse> result = medicalHistoryService.findAllByPatient(PATIENT_ID);

            // then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertSame(summaryOne, result.get(0));
            assertSame(summaryTwo, result.get(1));
        }

        @Test
        @DisplayName("Should return empty list when patient exists but has no medical histories")
        void shouldReturnEmptyListWhenPatientHasNoMedicalHistories() {

            // given
            when( patientService.getTenantIdOrThrow(PATIENT_ID) )
                    .thenReturn(TENANT_ID);
            when( medicalHistoryRepository.findAllByPatientIdOrderByStartDateDesc(PATIENT_ID) )
                    .thenReturn(List.of());

            // when
            List<MedicalHistorySummaryResponse> result = medicalHistoryService.findAllByPatient(PATIENT_ID);

            // then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(mapper, never()).toSummaryResponse(any(), anyInt(), anyInt(), anyInt());
            verify(medicalHistoryRepository, never()).countToothRecordsByMedicalHistoryId(any());
            verify(medicalHistoryRepository, never()).countAllergiesByMedicalHistoryId(any());
            verify(medicalHistoryRepository, never()).countExamsByMedicalHistoryId(any());
        }

        @Test
        @DisplayName("Should preserve descending order returned by the repository without resorting")
        void shouldPreserveDescendingOrderReturnedByRepository() {

            // given — repository already returns DESC order; service must not alter it
            MedicalHistory newest = buildMedicalHistory(20L, LocalDate.of(2025, 6, 1), null);
            MedicalHistory middle = buildMedicalHistory(21L, LocalDate.of(2025, 3, 15), null);
            MedicalHistory oldest = buildMedicalHistory(22L, LocalDate.of(2024, 1, 10), null);

            MedicalHistorySummaryResponse summaryNewest = buildSummary(20L, LocalDate.of(2025, 6, 1));
            MedicalHistorySummaryResponse summaryMiddle = buildSummary(21L, LocalDate.of(2025, 3, 15));
            MedicalHistorySummaryResponse summaryOldest = buildSummary(22L, LocalDate.of(2024, 1, 10));

            when(patientService.getTenantIdOrThrow(PATIENT_ID)).thenReturn(TENANT_ID);
            when(medicalHistoryRepository.findAllByPatientIdOrderByStartDateDesc(PATIENT_ID))
                    .thenReturn(List.of(newest, middle, oldest));

            stubCountsFor(newest, 0, 0, 0);
            stubCountsFor(middle, 0, 0, 0);
            stubCountsFor(oldest, 0, 0, 0);

            when(mapper.toSummaryResponse(newest, 0, 0, 0)).thenReturn(summaryNewest);
            when(mapper.toSummaryResponse(middle, 0, 0, 0)).thenReturn(summaryMiddle);
            when(mapper.toSummaryResponse(oldest, 0, 0, 0)).thenReturn(summaryOldest);

            // when
            List<MedicalHistorySummaryResponse> result = medicalHistoryService.findAllByPatient(PATIENT_ID);

            // then
            assertEquals(3, result.size());
            assertSame(summaryNewest, result.get(0));
            assertSame(summaryMiddle, result.get(1));
            assertSame(summaryOldest, result.get(2));
        }

        @Test
        @DisplayName("Should map correctly when editedBy is null on a medical history")
        void shouldMapCorrectlyWhenEditedByIsNull() {

            // given — historyOne was built with editedBy = null
            MedicalHistory historyWithNoEditor = buildMedicalHistory(30L, LocalDate.of(2025, 4, 1), null);

            MedicalHistorySummaryResponse summaryWithNullEditor = buildSummary(30L, LocalDate.of(2025, 4, 1));

            when(patientService.getTenantIdOrThrow(PATIENT_ID))
                    .thenReturn(TENANT_ID);

            when(medicalHistoryRepository.findAllByPatientIdOrderByStartDateDesc(PATIENT_ID))
                    .thenReturn(List.of(historyWithNoEditor));

            stubCountsFor(historyWithNoEditor, 4, 1, 0);

            when(mapper.toSummaryResponse(historyWithNoEditor, 4, 1, 0)).thenReturn(summaryWithNullEditor);

            // when
            List<MedicalHistorySummaryResponse> result = medicalHistoryService.findAllByPatient(PATIENT_ID);

            // then
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(mapper, times(1)).toSummaryResponse(historyWithNoEditor, 4, 1, 0);
        }

        // ── Exceptions ───────────────────────────────────────────────────────────

        @Test
        @DisplayName("Should throw PatientNotFoundException when patient does not exist in the system")
        void shouldThrowPatientNotFoundExceptionWhenPatientDoesNotExist() {
            // given
            when(patientService.getTenantIdOrThrow(PATIENT_ID))
                    .thenThrow(new PatientNotFoundException("The patient with this id: " + PATIENT_ID + " was not found"));

            // when
            assertThrows(
                    PatientNotFoundException.class,
                    () -> medicalHistoryService.findAllByPatient(PATIENT_ID)
            );

            // then
            verifyNoInteractions(medicalHistoryRepository, mapper);
        }

        @Test
        @DisplayName("Should throw PatientNotFoundException when patient belongs to a different tenant — no cross-tenant leakage")
        void shouldThrowPatientNotFoundExceptionWhenPatientBelongsToDifferentTenant() {
            // given — patient exists but is registered under a different tenant
            when(patientService.getTenantIdOrThrow(PATIENT_ID)).thenReturn(OTHER_TENANT_ID);

            // when
            PatientNotFoundException exception = assertThrows(
                    PatientNotFoundException.class,
                    () -> medicalHistoryService.findAllByPatient(PATIENT_ID)
            );

            // then — 404 semantics, never 403: prevents multi-tenancy information leakage
            assertNotNull(exception.getMessage());
            verifyNoInteractions(medicalHistoryRepository, mapper);
        }

        // ── Flow verification ────────────────────────────────────────────────────

        @Test
        @DisplayName("Should invoke the repository exactly once for the given patientId")
        void shouldInvokeRepositoryExactlyOnce() {
            // given
            when(patientService.getTenantIdOrThrow(PATIENT_ID)).thenReturn(TENANT_ID);
            when(medicalHistoryRepository.findAllByPatientIdOrderByStartDateDesc(PATIENT_ID))
                    .thenReturn(List.of());

            // when
            medicalHistoryService.findAllByPatient(PATIENT_ID);

            // then
            verify(medicalHistoryRepository, times(1))
                    .findAllByPatientIdOrderByStartDateDesc(PATIENT_ID);
            verifyNoMoreInteractions(medicalHistoryRepository);
        }

        @Test
        @DisplayName("Should execute all three count queries once per medical history when there are two records")
        void shouldExecuteThreeCountQueriesPerMedicalHistoryForTwoRecords() {
            // given
            when(patientService.getTenantIdOrThrow(PATIENT_ID)).thenReturn(TENANT_ID);
            when(medicalHistoryRepository.findAllByPatientIdOrderByStartDateDesc(PATIENT_ID))
                    .thenReturn(List.of(historyOne, historyTwo));

            stubCountsFor(historyOne, 2, 1, 3);
            stubCountsFor(historyTwo, 0, 0, 1);

            when(mapper.toSummaryResponse(eq(historyOne), anyInt(), anyInt(), anyInt())).thenReturn(summaryOne);
            when(mapper.toSummaryResponse(eq(historyTwo), anyInt(), anyInt(), anyInt())).thenReturn(summaryTwo);

            // when
            medicalHistoryService.findAllByPatient(PATIENT_ID);

            // then — 2 records × 3 count queries = 6 total count invocations
            verify(medicalHistoryRepository, times(2)).countToothRecordsByMedicalHistoryId(any());
            verify(medicalHistoryRepository, times(2)).countAllergiesByMedicalHistoryId(any());
            verify(medicalHistoryRepository, times(2)).countExamsByMedicalHistoryId(any());
        }

        @Test
        @DisplayName("Should pass the exact counts from the repository to the mapper for each medical history")
        void shouldPassExactCountsToMapper() {
            // given
            int expectedToothCount  = 7;
            int expectedAllergyCount = 3;
            int expectedExamCount   = 2;

            when(patientService.getTenantIdOrThrow(PATIENT_ID)).thenReturn(TENANT_ID);
            when(medicalHistoryRepository.findAllByPatientIdOrderByStartDateDesc(PATIENT_ID))
                    .thenReturn(List.of(historyOne));

            stubCountsFor(historyOne, expectedToothCount, expectedAllergyCount, expectedExamCount);
            when(mapper.toSummaryResponse(historyOne, expectedToothCount, expectedAllergyCount, expectedExamCount))
                    .thenReturn(summaryOne);

            // when
            medicalHistoryService.findAllByPatient(PATIENT_ID);

            // then — verifies the exact argument values forwarded to the mapper
            verify(mapper, times(1))
                    .toSummaryResponse(historyOne, expectedToothCount, expectedAllergyCount, expectedExamCount);
        }

        @Test
        @DisplayName("Should invoke the mapper exactly once per medical history returned by the repository")
        void shouldInvokeMapperExactlyOncePerMedicalHistory() {
            // given
            when(patientService.getTenantIdOrThrow(PATIENT_ID)).thenReturn(TENANT_ID);
            when(medicalHistoryRepository.findAllByPatientIdOrderByStartDateDesc(PATIENT_ID))
                    .thenReturn(List.of(historyOne, historyTwo));

            stubCountsFor(historyOne, 1, 0, 0);
            stubCountsFor(historyTwo, 0, 1, 0);

            when(mapper.toSummaryResponse(eq(historyOne), anyInt(), anyInt(), anyInt())).thenReturn(summaryOne);
            when(mapper.toSummaryResponse(eq(historyTwo), anyInt(), anyInt(), anyInt())).thenReturn(summaryTwo);

            // when
            medicalHistoryService.findAllByPatient(PATIENT_ID);

            // then
            verify(mapper, times(1)).toSummaryResponse(eq(historyOne), anyInt(), anyInt(), anyInt());
            verify(mapper, times(1)).toSummaryResponse(eq(historyTwo), anyInt(), anyInt(), anyInt());
            verifyNoMoreInteractions(mapper);
        }

        @Test
        @DisplayName("Should validate tenant before querying the repository — strict execution order")
        void shouldValidateTenantBeforeQueryingRepository() {

            // given
            when( patientService.getTenantIdOrThrow(PATIENT_ID) )
                    .thenReturn(TENANT_ID);

            when( medicalHistoryRepository.findAllByPatientIdOrderByStartDateDesc(PATIENT_ID) )
                    .thenReturn(List.of());

            // when
            medicalHistoryService.findAllByPatient(PATIENT_ID);

            // then — patientService must be called strictly before the repository
            InOrder inOrder = inOrder(patientService, medicalHistoryRepository);

            inOrder.verify(patientService).getTenantIdOrThrow(PATIENT_ID);

            inOrder.verify(medicalHistoryRepository).findAllByPatientIdOrderByStartDateDesc(PATIENT_ID);
        }

        // ── Private helpers ──────────────────────────────────────────────────────

        private MedicalHistory buildMedicalHistory(Long id, LocalDate startDate, UserProfile editedByProfile) {
            MedicalHistory mh = new MedicalHistory();
            mh.setId(id);
            mh.setStartDate(startDate);
            mh.setOdontogramType(OdontogramType.ADULT);
            mh.setHasAllergies(false);
            mh.setDentist(dentist);
            mh.setPatient(patient);
            mh.setEditedBy(editedByProfile);
            return mh;
        }

        private MedicalHistorySummaryResponse buildSummary(Long id, LocalDate startDate) {
            return new MedicalHistorySummaryResponse(
                    id,
                    startDate,
                    OdontogramType.ADULT,
                    null,
                    null,
                    false,
                    null,
                    new MedicalHistorySummaryResponse.DentistSummary(DENTIST_ID, "Juan Pérez"),
                    null,
                    0,
                    0,
                    0
            );
        }

        private void stubCountsFor(MedicalHistory mh, int toothCount, int allergyCount, int examCount) {
            when(medicalHistoryRepository.countToothRecordsByMedicalHistoryId(mh.getId())).thenReturn(toothCount);
            when(medicalHistoryRepository.countAllergiesByMedicalHistoryId(mh.getId())).thenReturn(allergyCount);
            when(medicalHistoryRepository.countExamsByMedicalHistoryId(mh.getId())).thenReturn(examCount);
        }
    }

    @Nested
    class GetMedicalHistoryDetailTests {

        private static final String TENANT_ID          = "tenant-abc-123";
        private static final String OTHER_TENANT_ID    = "tenant-xyz-999";
        private static final Long   MEDICAL_HISTORY_ID = 42L;

        @BeforeEach
        void setUpTenant() {
            TenantContext.set(TENANT_ID);
        }

        @AfterEach
        void clearTenant() {
            TenantContext.clear();
        }

        // ── Helper factories ───────────────────────────────────────────────────

        private MedicalHistory buildDetailHistory(Long id, boolean hasAllergies, List<PatientAllergy> allergies) {
            MedicalHistory mh = new MedicalHistory();
            mh.setId(id);
            mh.setStartDate(LocalDate.of(2025, 6, 1));
            mh.setOdontogramType(OdontogramType.ADULT);
            mh.setHasAllergies(hasAllergies);
            mh.setDentist(dentist);
            mh.setPatient(patient);
            mh.setAllergies(allergies != null ? new ArrayList<>(allergies) : null);
            return mh;
        }

        private MedicalHistoryDetailResponse stubDetailResponse(Long historyId) {

            return new MedicalHistoryDetailResponse(historyId,
                                                    LocalDate.of(2025, 6, 1),
                                                    OdontogramType.ADULT,
                                                    null, null, false, null,
                                                    null, null, null,
                                                    List.of(), List.of(), List.of() );
        }

        private void stubHappyPath(MedicalHistory mh) {

            when(patientService.getTenantIdOrThrow(PATIENT_ID))
                    .thenReturn(TENANT_ID);

            when(patientService.findPatientById(PATIENT_ID))
                    .thenReturn(patient);

            when(medicalHistoryRepository.findMedicalHistoryBaseById(MEDICAL_HISTORY_ID))
                    .thenReturn(Optional.of(mh));
        }

        // ── Happy path ─────────────────────────────────────────────────────────

        @Test
        @DisplayName("Should return full detail response when all collections are populated and editedBy is present")
        void shouldReturnFullDetailWhenAllCollectionsArePopulated() {

            // given
            AllergyCatalog catalog = AllergyCatalog.builder().id(1L).name("Penicilina").active(true).build();

            PatientAllergy pa      = PatientAllergy.builder().allergy(catalog).build();

            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, true, List.of(pa));

            mh.setEditedBy(userProfile);

            PatientAllergyDetailResponse allergyResponse = new PatientAllergyDetailResponse(1L, "Reacción severa", 1L, "Penicilina", true);

            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when( patientAllergyMapper.toPatientAllergyDetailResponse(pa) ).thenReturn(allergyResponse);
            when( mapper.buildMedicalHistoryDetailResponse( eq(mh), eq(patient), anyList() ) ).thenReturn(expectedResponse);

            // when
            MedicalHistoryDetailResponse actual = medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            assertNotNull(actual);
            assertSame(expectedResponse, actual);
        }

        @Test
        @DisplayName("Should return detail response normally when editedBy is null — TC-06")
        void shouldReturnDetailWhenEditedByIsNull() {

            // given
            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, false, new ArrayList<>());
            mh.setEditedBy(null);
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when(mapper.buildMedicalHistoryDetailResponse( mh, patient, List.of() ))
                    .thenReturn(expectedResponse);

            // when
            MedicalHistoryDetailResponse actual = medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            assertNotNull(actual);
            assertSame(expectedResponse, actual);
            verifyNoInteractions(patientAllergyMapper);
        }

        @Test
        @DisplayName("Should return detail response normally when editedBy is present — TC-07")
        void shouldReturnDetailWhenEditedByIsPresent() {

            // given
            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, false, new ArrayList<>());
            mh.setEditedBy(userProfile);
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when( mapper.buildMedicalHistoryDetailResponse( mh, patient, List.of() ) )
                    .thenReturn(expectedResponse);

            // when
            MedicalHistoryDetailResponse actual = medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            assertNotNull(actual);
            assertSame(expectedResponse, actual);
        }

        // ── Edge cases (allergies) ─────────────────────────────────────────────

        @Test
        @DisplayName("Should pass empty allergy list to mapper when hasAllergies is false — RN-05, TC-03")
        void shouldPassEmptyAllergyListWhenHasAllergiesIsFalse() {

            // given
            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, false, new ArrayList<>());
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when( mapper.buildMedicalHistoryDetailResponse( mh, patient, List.of() ) )
                    .thenReturn( expectedResponse );

            // when
            medicalHistoryService.getMedicalHistoryDetail( PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME );

            // then — patientAllergyMapper is never invoked when hasAllergies = false
            verify( patientAllergyMapper, never() ).toPatientAllergyDetailResponse( any(PatientAllergy.class) );
            verify( mapper, times(1) ).buildMedicalHistoryDetailResponse( mh, patient, List.of() );
        }

        @Test
        @DisplayName("Should pass empty allergy list when hasAllergies is true but list is empty — EC-03, TC-04")
        void shouldPassEmptyAllergyListWhenHasAllergiesIsTrueButListIsEmpty() {

            // given
            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, true, new ArrayList<>());
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when(mapper.buildMedicalHistoryDetailResponse(mh, patient, List.of()))
                    .thenReturn(expectedResponse);

            // when
            medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            verify( patientAllergyMapper, never() ).toPatientAllergyDetailResponse( any( PatientAllergy.class ) );
            verify( mapper, times(1) ).buildMedicalHistoryDetailResponse( mh, patient, List.of() );
        }

        @Test
        @DisplayName("Should pass empty allergy list when hasAllergies is true but allergy list is null — defensive")
        void shouldPassEmptyAllergyListWhenHasAllergiesIsTrueButListIsNull() {

            // given — allergyEntities == null → resolveAllergies returns List.of()
            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, true, null);
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when( mapper.buildMedicalHistoryDetailResponse( mh, patient, List.of() ) )
                    .thenReturn(expectedResponse);

            // when
            medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            verify( patientAllergyMapper, never() ).toPatientAllergyDetailResponse( any(PatientAllergy.class) );
            verify( mapper, times(1) ).buildMedicalHistoryDetailResponse( mh, patient, List.of() );
        }

        @Test
        @DisplayName("Should silently ignore inconsistent allergy records when hasAllergies is false — TC-30, RN-05")
        void shouldIgnoreInconsistentAllergyRecordsWhenHasAllergiesIsFalse() {

            // given — hasAllergies = false but DB has stale allergy rows (write-time inconsistency)
            AllergyCatalog catalog    = AllergyCatalog.builder().id(2L).name("Látex").active(true).build();
            PatientAllergy staleRecord = PatientAllergy.builder().allergy(catalog).build();

            MedicalHistory mh = buildDetailHistory( MEDICAL_HISTORY_ID, false, List.of(staleRecord) );
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when(mapper.buildMedicalHistoryDetailResponse( mh, patient, List.of() ))
                    .thenReturn(expectedResponse);

            // when
            medicalHistoryService.getMedicalHistoryDetail( PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME );

            // then — stale records are silently dropped; mapper receives empty list
            verify( patientAllergyMapper, never() ).toPatientAllergyDetailResponse( any( PatientAllergy.class ) );
            verify( mapper, times(1) ).buildMedicalHistoryDetailResponse( mh, patient, List.of() );
        }

        @Test
        @DisplayName("Should map all allergy entities through patientAllergyMapper when list is non-empty — TC-31")
        void shouldMapAllAllergyEntitiesWhenListIsNotEmpty() {

            // given
            AllergyCatalog c1 = AllergyCatalog.builder().id(1L).name("Penicilina").active(true).build();
            AllergyCatalog c2 = AllergyCatalog.builder().id(4L).name("Látex").active(true).build();

            PatientAllergy pa1 = PatientAllergy.builder().allergy(c1).build();
            PatientAllergy pa2 = PatientAllergy.builder().allergy(c2).build();

            PatientAllergyDetailResponse r1 = new PatientAllergyDetailResponse(1L, null,    1L, "Penicilina", true);
            PatientAllergyDetailResponse r2 = new PatientAllergyDetailResponse(2L, "Notas", 4L, "Látex",     true);

            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, true, List.of(pa1, pa2));
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when( patientAllergyMapper.toPatientAllergyDetailResponse( pa1 ) ).thenReturn( r1 );
            when( patientAllergyMapper.toPatientAllergyDetailResponse( pa2 ) ).thenReturn( r2 );

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<PatientAllergyDetailResponse>> allergyCaptor = ArgumentCaptor.forClass(List.class);

            when(mapper.buildMedicalHistoryDetailResponse( eq( mh ), eq(patient), allergyCaptor.capture() ) )
                    .thenReturn(expectedResponse);

            // when
            medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            verify(patientAllergyMapper, times(1)).toPatientAllergyDetailResponse(pa1);
            verify(patientAllergyMapper, times(1)).toPatientAllergyDetailResponse(pa2);

            List<PatientAllergyDetailResponse> captured = allergyCaptor.getValue();
            assertEquals(2, captured.size());
            assertTrue(captured.contains(r1));
            assertTrue(captured.contains(r2));
        }

        // ── Edge cases (collections empty) ────────────────────────────────────

        @Test
        @DisplayName("Should proceed and return response when toothRecords collection is empty — TC-02")
        void shouldProceedWhenToothRecordsIsEmpty() {

            // given
            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, false, new ArrayList<>());
            mh.setToothRecords(new ArrayList<>());
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when(mapper.buildMedicalHistoryDetailResponse(mh, patient, List.of()))
                    .thenReturn(expectedResponse);

            // when
            MedicalHistoryDetailResponse actual = medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            assertNotNull(actual);
            verify(mapper, times(1)).buildMedicalHistoryDetailResponse(mh, patient, List.of());
        }

        @Test
        @DisplayName("Should proceed and return response when complementaryExams collection is empty — TC-05")
        void shouldProceedWhenComplementaryExamsIsEmpty() {

            // given
            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, false, new ArrayList<>());
            mh.setExams(new ArrayList<>());
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when( mapper.buildMedicalHistoryDetailResponse( mh, patient, List.of() ) )
                    .thenReturn(expectedResponse);

            // when
            MedicalHistoryDetailResponse actual = medicalHistoryService.getMedicalHistoryDetail( PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME );

            // then
            assertNotNull(actual);
            verify( mapper, times(1) ).buildMedicalHistoryDetailResponse( mh, patient, List.of() );
        }

        @Test
        @DisplayName("Should forward null optional text fields to mapper as-is without throwing — TC-29")
        void shouldHandleNullOptionalTextFieldsInMedicalHistory() {

            // given
            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, false, new ArrayList<>());
            mh.setPastMedicalHistory(null);
            mh.setObservations(null);
            mh.setDailyMedication(null);
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when( mapper.buildMedicalHistoryDetailResponse( mh, patient, List.of() ))
                    .thenReturn(expectedResponse);

            // when
            MedicalHistoryDetailResponse actual = medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then — null optional fields are forwarded to the mapper unchanged; no exception is thrown
            assertNotNull(actual);
            verify( mapper, times(1) ).buildMedicalHistoryDetailResponse( mh, patient, List.of() );
        }

        // ── Exceptions ─────────────────────────────────────────────────────────

        @Test
        @DisplayName("Should throw PatientNotFoundException when patient belongs to a different tenant — TC-23")
        void shouldThrowPatientNotFoundWhenPatientBelongsToDifferentTenant() {

            // given
            when(patientService.getTenantIdOrThrow(PATIENT_ID)).thenReturn(OTHER_TENANT_ID);

            // when
            Executable executable = () -> medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            PatientNotFoundException exception = assertThrows(PatientNotFoundException.class, executable);
            assertNotNull(exception.getMessage());

            verify(patientService, never()).findPatientById(any());
            verifyNoInteractions(medicalHistoryRepository, mapper, patientAllergyMapper);
        }

        @Test
        @DisplayName("Should propagate PatientNotFoundException when getTenantIdOrThrow itself throws — patient absent from all tenants")
        void shouldPropagatePatientNotFoundWhenGetTenantIdOrThrowThrows() {

            // given
            when(patientService.getTenantIdOrThrow(PATIENT_ID))
                    .thenThrow(new PatientNotFoundException("The patient with this id: " + PATIENT_ID + " was not found"));

            // when
            Executable executable = () -> medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            assertThrows(PatientNotFoundException.class, executable);

            verify(patientService, never()).findPatientById(any());
            verifyNoInteractions(medicalHistoryRepository, mapper, patientAllergyMapper);
        }

        @Test
        @DisplayName("Should throw PatientNotFoundException when findPatientById throws — TC-25")
        void shouldThrowPatientNotFoundWhenFindPatientByIdThrows() {

            // given
            when(patientService.getTenantIdOrThrow(PATIENT_ID))
                    .thenReturn(TENANT_ID);

            when(patientService.findPatientById(PATIENT_ID))
                    .thenThrow(new PatientNotFoundException("The patient with this id: " + PATIENT_ID + " was not found"));

            // when
            Executable executable = () ->
                    medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            assertThrows(PatientNotFoundException.class, executable);
            verifyNoInteractions(medicalHistoryRepository, mapper, patientAllergyMapper);
        }

        @Test
        @DisplayName("Should throw MedicalHistoryNotFoundException when history does not exist in the tenant — TC-24")
        void shouldThrowMedicalHistoryNotFoundWhenHistoryDoesNotExist() {

            // given
            when(patientService.getTenantIdOrThrow(PATIENT_ID))
                    .thenReturn(TENANT_ID);

            when(patientService.findPatientById(PATIENT_ID))
                    .thenReturn(patient);

            when(medicalHistoryRepository.findMedicalHistoryBaseById(MEDICAL_HISTORY_ID))
                    .thenReturn(Optional.empty());

            // when
            Executable executable = () -> medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            assertThrows(MedicalHistoryNotFoundException.class, executable);

            verify(medicalHistoryRepository, times(1)).findMedicalHistoryBaseById(MEDICAL_HISTORY_ID);
            verify(medicalHistoryRepository, never()).findWithToothRecords(any());
            verify(medicalHistoryRepository, never()).findWithAllergies(any());
            verify(medicalHistoryRepository, never()).findWithExams(any());
            verify(mapper, never()).buildMedicalHistoryDetailResponse(any(), any(), any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when medical history does not belong to the requested patient — TC-26, TC-28")
        void shouldThrowResourceNotFoundWhenHistoryDoesNotBelongToPatient() {

            // given — history's owner patient has id 99L; request is for patient id 7L (PATIENT_ID)
            Patient anotherPatient = new Patient();
            anotherPatient.setId_patient(99L);
            anotherPatient.setName("other");
            anotherPatient.setSurname("patient");

            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, false, new ArrayList<>());
            mh.setPatient(anotherPatient);

            when(patientService.getTenantIdOrThrow(PATIENT_ID))
                    .thenReturn(TENANT_ID);

            when(patientService.findPatientById(PATIENT_ID))
                    .thenReturn(patient);

            when(medicalHistoryRepository.findMedicalHistoryBaseById(MEDICAL_HISTORY_ID))
                    .thenReturn(Optional.of(mh));

            // when
            Executable executable = () -> medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            assertThrows( ResourceNotFoundException.class, executable );

            // collection-loading methods are invoked before ownership validation in the service
            verify( medicalHistoryRepository, times(1) ).findWithToothRecords(MEDICAL_HISTORY_ID);
            verify( medicalHistoryRepository, times(1) ).findWithAllergies(MEDICAL_HISTORY_ID);
            verify( medicalHistoryRepository, times(1) ).findWithExams(MEDICAL_HISTORY_ID);

            verify(mapper, never()).buildMedicalHistoryDetailResponse(any(), any(), any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when history belongs to another patient of the same tenant — TC-27")
        void shouldThrowResourceNotFoundWhenHistoryBelongsToAnotherPatientSameTenant() {

            // given — patient 8L and patient 7L share the same tenant; history is owned by 8L
            Patient otherPatient = new Patient();
            otherPatient.setId_patient(8L);
            otherPatient.setName("Lucía");
            otherPatient.setSurname("Fernández");

            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, false, new ArrayList<>());
            mh.setPatient(otherPatient);

            when(patientService.getTenantIdOrThrow(PATIENT_ID))
                    .thenReturn(TENANT_ID);

            when(patientService.findPatientById(PATIENT_ID))
                    .thenReturn(patient);

            when(medicalHistoryRepository.findMedicalHistoryBaseById(MEDICAL_HISTORY_ID))
                    .thenReturn(Optional.of(mh));

            // when
            Executable executable = () ->
                    medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then — always 404, never 403: prevents multi-tenancy information leakage
            assertThrows(ResourceNotFoundException.class, executable);
            verify(mapper, never()).buildMedicalHistoryDetailResponse(any(), any(), any());
        }

        // ── Flow verification ──────────────────────────────────────────────────

        @Test
        @DisplayName("Should validate tenant strictly before loading the patient from the service — strict execution order")
        void shouldValidateTenantBeforeLoadingPatient() {

            // given
            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, false, new ArrayList<>());
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when(mapper.buildMedicalHistoryDetailResponse(mh, patient, List.of()))
                    .thenReturn(expectedResponse);

            // when
            medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then — getTenantIdOrThrow must precede findPatientById
            InOrder inOrder = inOrder(patientService);
            inOrder.verify(patientService).getTenantIdOrThrow(PATIENT_ID);
            inOrder.verify(patientService).findPatientById(PATIENT_ID);
        }

        @Test
        @DisplayName("Should call all three collection-loading repository methods exactly once per invocation")
        void shouldCallAllThreeCollectionLoadMethodsExactlyOnce() {

            // given
            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, false, new ArrayList<>());
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when(mapper.buildMedicalHistoryDetailResponse(mh, patient, List.of()))
                    .thenReturn(expectedResponse);

            // when
            medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            verify(medicalHistoryRepository, times(1)).findWithToothRecords(MEDICAL_HISTORY_ID);
            verify(medicalHistoryRepository, times(1)).findWithAllergies(MEDICAL_HISTORY_ID);
            verify(medicalHistoryRepository, times(1)).findWithExams(MEDICAL_HISTORY_ID);
        }

        @Test
        @DisplayName("Should load all three collections strictly after finding the medical history base — strict execution order")
        void shouldLoadCollectionsAfterFindingMedicalHistoryBase() {

            // given
            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, false, new ArrayList<>());
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when(mapper.buildMedicalHistoryDetailResponse(mh, patient, List.of()))
                    .thenReturn(expectedResponse);

            // when
            medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then — base query must precede all EntityGraph collection fetches
            InOrder inOrder = inOrder(medicalHistoryRepository);
            inOrder.verify(medicalHistoryRepository).findMedicalHistoryBaseById(MEDICAL_HISTORY_ID);
            inOrder.verify(medicalHistoryRepository).findWithToothRecords(MEDICAL_HISTORY_ID);
            inOrder.verify(medicalHistoryRepository).findWithAllergies(MEDICAL_HISTORY_ID);
            inOrder.verify(medicalHistoryRepository).findWithExams(MEDICAL_HISTORY_ID);
        }

        @Test
        @DisplayName("Should pass the exact MedicalHistory instance, Patient and resolved allergies to the mapper")
        void shouldPassExactArgumentsToMapper() {

            // given
            AllergyCatalog catalog = AllergyCatalog.builder().id(1L).name("Penicilina").active(true).build();
            PatientAllergy pa      = PatientAllergy.builder().allergy(catalog).build();
            PatientAllergyDetailResponse allergyDetailResponse =
                    new PatientAllergyDetailResponse(10L, "Nota test", 1L, "Penicilina", true);

            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, true, List.of(pa));
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when(patientAllergyMapper.toPatientAllergyDetailResponse(pa)).thenReturn(allergyDetailResponse);

            ArgumentCaptor<MedicalHistory> historyCaptor = ArgumentCaptor.forClass(MedicalHistory.class);
            ArgumentCaptor<Patient>        patientCaptor = ArgumentCaptor.forClass(Patient.class);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<PatientAllergyDetailResponse>> allergyCaptor = ArgumentCaptor.forClass(List.class);

            when( mapper.buildMedicalHistoryDetailResponse( historyCaptor.capture(),  patientCaptor.capture(), allergyCaptor.capture()) )
                    .thenReturn(expectedResponse);

            // when
            medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            assertSame( mh,      historyCaptor.getValue() );
            assertSame( patient, patientCaptor.getValue() );

            List<PatientAllergyDetailResponse> resolvedAllergies = allergyCaptor.getValue();
            assertEquals(1, resolvedAllergies.size());
            assertSame(allergyDetailResponse, resolvedAllergies.get(0));
        }

        @Test
        @DisplayName("Should invoke the mapper exactly once and return its result unchanged")
        void shouldInvokeMapperExactlyOnceAndReturnItsResult() {

            // given
            MedicalHistory mh = buildDetailHistory(MEDICAL_HISTORY_ID, false, new ArrayList<>());
            MedicalHistoryDetailResponse expectedResponse = stubDetailResponse(MEDICAL_HISTORY_ID);

            stubHappyPath(mh);
            when(mapper.buildMedicalHistoryDetailResponse(mh, patient, List.of()))
                    .thenReturn(expectedResponse);

            // when
            MedicalHistoryDetailResponse actual = medicalHistoryService.getMedicalHistoryDetail(PATIENT_ID, MEDICAL_HISTORY_ID, USERNAME);

            // then
            verify(mapper, times(1)).buildMedicalHistoryDetailResponse(any(), any(), any());
            verifyNoMoreInteractions(mapper);
            assertSame(expectedResponse, actual);
        }
    }
}