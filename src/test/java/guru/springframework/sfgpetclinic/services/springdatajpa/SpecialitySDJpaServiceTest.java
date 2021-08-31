package guru.springframework.sfgpetclinic.services.springdatajpa;

import guru.springframework.sfgpetclinic.model.Speciality;
import guru.springframework.sfgpetclinic.repositories.SpecialtyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecialitySDJpaServiceTest {

    @Mock (lenient = true)
    SpecialtyRepository specialtyRepository;

    @InjectMocks
    SpecialitySDJpaService service;

    @Test
    void testDeleteByObject() {
        // given
        Speciality speciality = new Speciality();

        // when
        service.delete(speciality);

        // then
        then(specialtyRepository).should().delete(any(Speciality.class));
    }

    @Test
    void findByIdTest() {

        // given
        Speciality speciality = new Speciality();
        given(specialtyRepository.findById(1L)).willReturn(Optional.of(speciality));

        // when
        Speciality foundSpecialty = service.findById(1L);

        // then
        assertThat(foundSpecialty).isNotNull();
        // added timeout to demonstrate the use of the timeout method
        // Note: timeout() appears not to work. If I pass 1 millis then it still completes normally.
        then(specialtyRepository).should(timeout(100)).findById(anyLong());
        then(specialtyRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void deleteById() {
        // when
        service.deleteById(1l);
        service.deleteById(1l);

        // then
        // added timeout to show how it can be chained in with other should-methods
        then(specialtyRepository).should(timeout(100).times(2)).deleteById(1L);
    }

    @Test
    void deleteByIdAtLeast() {
        // when
        service.deleteById(1l);
        service.deleteById(1l);

        // then
        // added timeout to show how it can be chained in with other should-methods
        then(specialtyRepository).should(timeout(1000).atLeastOnce()).deleteById(1L);
    }

    @Test
    void deleteByIdAtMost() {
        // when
        service.deleteById(1l);
        service.deleteById(1l);

        // then
        // cannot add timeout in the presence of atMost()
        then(specialtyRepository).should(atMost(5)).deleteById(1L);
    }

    @Test
    void deleteByIdNever() {
        // when
        service.deleteById(1l);
        service.deleteById(1l);

        // then
        then(specialtyRepository).should(timeout(100).atLeastOnce()).deleteById(1L);
        then(specialtyRepository).should(never()).deleteById(5L);
    }

    @Test
    void testDelete() {
        // when
        service.delete(new Speciality());

        // then
        then(specialtyRepository).should().delete(any());
    }
    
    @Test
    void testDoThrow() {
        doThrow(new RuntimeException("boom")).when(specialtyRepository).delete(any());
        assertThrows(RuntimeException.class, () -> specialtyRepository.delete(new Speciality()));
        verify(specialtyRepository).delete(any());
    }

    /**
     * BDD style test for a thrown exception
     */
    @Test
    void testFindByIdThrows() {
        given(specialtyRepository.findById(1L)).willThrow(new RuntimeException("boom"));
        assertThrows(RuntimeException.class, () -> service.findById(1L));
        then(specialtyRepository).should().findById(1L);
    }

    /**
     * BDD style with willThrow() as the given
     */
    @Test
    void testDeleteBDD() {
        willThrow(new RuntimeException("boom")).given(specialtyRepository).delete(any());
        assertThrows(RuntimeException.class, () -> service.delete(new Speciality()));
        then(specialtyRepository).should().delete(any());
    }

    /**
     * Use a lambda matcher when you need to look into an object and access its properties.
     */
    @Test
    void testSaveLambda() {
        // given
        final String MATCH_ME = "MATCH_ME";
        Speciality speciality = new Speciality();
        speciality.setDescription(MATCH_ME);

        Speciality savedSpeciality = new Speciality();
        savedSpeciality.setId(1L);

        // Need mock to only return on match of MATCH_ME
        given(specialtyRepository.save(argThat(argument -> argument.getDescription().equals(MATCH_ME)))).willReturn(savedSpeciality);

        // when
        Speciality returnedSpeciality = service.save(speciality);

        // then
        assertThat(returnedSpeciality.getId()).isEqualTo(1L);

    }

    @Test
    void testSaveLambdaNoMatch() {
        // given
        final String MATCH_ME = "MATCH_ME";
        Speciality speciality = new Speciality();
        speciality.setDescription("Not a match");

        Speciality savedSpeciality = new Speciality();
        savedSpeciality.setId(1L);

        // Need mock to only return on match of MATCH_ME
        // argThat is a "strict matcher." In this test, the lambda returns null so it fails.
        // One way to solve the problem is to modify the @Mock annotation to @Mock(Lenient=true).
        given(specialtyRepository.save(argThat(argument -> argument.getDescription().equals(MATCH_ME)))).willReturn(savedSpeciality);

        // when
        Speciality returnedSpeciality = service.save(speciality);

        // then
        assertNull(returnedSpeciality);

    }

}
