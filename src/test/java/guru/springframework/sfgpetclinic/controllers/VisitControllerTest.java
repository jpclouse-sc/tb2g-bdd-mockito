package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.model.Pet;
import guru.springframework.sfgpetclinic.model.Visit;
import guru.springframework.sfgpetclinic.services.PetService;
import guru.springframework.sfgpetclinic.services.VisitService;
import guru.springframework.sfgpetclinic.services.map.PetMapService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class VisitControllerTest {

    @Mock
    VisitService visitService;

    // There is no concrete class underneath PetService so we will use a @Spy
    /*@Mock
    PetService service;*/

    // Spy is a wrapper around the real object
    @Spy
    PetMapService service;

    @InjectMocks
    VisitController controller;

    @Test
    void loadPetWithVisit() {
        // given
        Map<String, Object> model = new HashMap<>();
        Pet pet = new Pet(12L);
        Pet pet3 = new Pet(3L);

        // This is necessary when we use @Spy
        service.save(pet);
        service.save(pet3);

        // For Spy we use .willCallRealMethod()
        given(service.findById(anyLong())).willCallRealMethod(); //.willReturn(pet);

        // when
        Visit visit = controller.loadPetWithVisit(12L, model);

        // then
        assertThat(visit).isNotNull();
        assertThat(pet).isNotNull();
        assertThat(visit.getPet().getId()).isEqualTo(12L);
        Mockito.verify(service, times(1)).findById(anyLong());
    }

    @Test
    void loadPetWithVisitWithStubbing() {
        // given
        Map<String, Object> model = new HashMap<>();
        Pet pet = new Pet(12L);
        Pet pet3 = new Pet(3L);

        // This is necessary when we use @Spy
        service.save(pet);
        service.save(pet3);

        // Stubbing here. The Spy will return pet3
        given(service.findById(anyLong())).willReturn(pet3);

        // when
        // The spy intercepts this. The given stub returns pet3 rather pet
        Visit visit = controller.loadPetWithVisit(12L, model);

        // then
        assertThat(visit).isNotNull();
        assertThat(pet).isNotNull();
        // pet3 is returned, not pet.
        assertThat(visit.getPet().getId()).isEqualTo(3L);
        Mockito.verify(service, times(1)).findById(anyLong());
    }
}
