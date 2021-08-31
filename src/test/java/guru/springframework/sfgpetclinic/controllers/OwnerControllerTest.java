package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.fauxspring.BindingResult;
import guru.springframework.sfgpetclinic.fauxspring.Model;
import guru.springframework.sfgpetclinic.model.Owner;
import guru.springframework.sfgpetclinic.services.OwnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    public static final String REDIRECT_OWNERS_5 = "redirect:/owners/5";

    /**
     * We have to use lenient=true here because some of the test methods don't need
     * the stub created in the @BeforeEach method. I suppose we could have
     * moved the code into a regular method and have the methods that use it call
     * the method.
     */
    @Mock(lenient = true)
    OwnerService service;

    @Mock
    BindingResult bindingResult;

    @Mock
    Model model;

    @InjectMocks
    OwnerController controller;

    /**
     * An annotation-based captor
     */
    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    /**
     * I'm not sure why this was put here, other than the fact that more than one
     * of the tests below can make use of it.
     */
    @BeforeEach
    void setUp() {
        given(service
                .findAllByLastNameLike(stringArgumentCaptor.capture()))
                .willAnswer(invocation -> {
                    List<Owner> owners = new ArrayList<>();
                    String name = invocation.getArgument(0);

                    if (name.equals("%Buck%")) {
                        owners.add(new Owner(1L, "Joe", "Buck"));
                        return owners;
                    } else if (name.equals("%DontFindMe%")) {
                        return owners;
                    } else if (name.equals("%FindMe%")) {
                        owners.add(new Owner(1L, "Joe", "Buck"));
                        owners.add(new Owner(1L, "Pete", "Boggs"));
                        return owners;
                    }

                    throw new RuntimeException("Invalid Argument");
                }
        );
    }

    @Test
    void processFindFormWildCardStringAnnotation() {
        // given
        Owner owner = new Owner(1L, "Joe", "Buck");

        // when
        String viewName = controller.processFindForm(owner, bindingResult, null);

        // then
        assertThat("%Buck%").isEqualTo(stringArgumentCaptor.getValue());
        assertThat("redirect:/owners/1").isEqualToIgnoringCase(viewName);
        verifyZeroInteractions(model);
    }

    @Test
    void processFindFormWildCardNotFound() {
        // given
        Owner owner = new Owner(1L, "Joe", "DontFindMe");

        // when
        String viewName = controller.processFindForm(owner, bindingResult, null);

        // then
        assertThat("%DontFindMe%").isEqualTo(stringArgumentCaptor.getValue());
        assertThat("owners/findOwners").isEqualToIgnoringCase(viewName);
        verifyZeroInteractions(model);
    }

    @Test
    void processFindFormWildCardFound() {
        // given
        Owner owner = new Owner(1L, "Joe", "FindMe");
        // This ensures that Mockito invokes the mock service first, then the mock model
        // in the inOrder.verify methods below (but why)?
        InOrder inOrder = inOrder(service, model);

        // when
        String viewName = controller.processFindForm(owner, bindingResult, model);

        // then
        assertThat("%FindMe%").isEqualTo(stringArgumentCaptor.getValue());
        assertThat("owners/ownersList").isEqualToIgnoringCase(viewName);

        // inOrder assertions
        inOrder.verify(service).findAllByLastNameLike(anyString());
        inOrder.verify(model, times(1)).addAttribute(anyString(), anyList());
        verifyNoMoreInteractions(model);
    }

    @Test
    void processCreationFormHasErrors() {
        // given
        Owner owner = new Owner(1L, "Joe", "Blow");
        given(bindingResult.hasErrors()).willReturn(true);

        // when
        String viewName = controller.processCreationForm(owner, bindingResult);

        // then
        assertThat(viewName).isEqualToIgnoringCase(OwnerController.VIEWS_OWNER_CREATE_OR_UPDATE_FORM);
    }

    @Test
    void processCreationFormNoErrors() {
        // given
        Owner owner = new Owner(5L, "Joe", "Blow");
        given(bindingResult.hasErrors()).willReturn(false);
        given(service.save(any())).willReturn(owner);

        // when
        String viewName = controller.processCreationForm(owner, bindingResult);

        // then
        assertThat(viewName).isEqualToIgnoringCase(REDIRECT_OWNERS_5);
    }
}
