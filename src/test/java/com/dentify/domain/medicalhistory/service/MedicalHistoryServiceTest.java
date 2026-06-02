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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
            CreateMedicalHistoryResponse actualResponse =
                    medicalHistoryService.createMedicalHistory(PATIENT_ID, USERNAME, request);

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

            when(patientService.getTenantIdOrThrow(PATIENT_ID)).thenReturn(TENANT_ID);
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

}