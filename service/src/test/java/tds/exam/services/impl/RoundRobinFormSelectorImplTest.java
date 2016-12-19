package tds.exam.services.impl;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Optional;

import tds.assessment.Algorithm;
import tds.assessment.Form;
import tds.assessment.Segment;
import tds.exam.services.FormSelector;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class RoundRobinFormSelectorImplTest {
    @Autowired
    private FormSelector formSelector;

    @Test
    public void shouldReturnFormsRoundRobin() {
        final String languageCode = "ENU";

        Form enuForm1 = new Form.Builder("formkey1")
            .withLanguage(languageCode)
            .build();
        Form enuForm2 = new Form.Builder("formkey2")
            .withLanguage(languageCode)
            .build();
        Form enuForm3 = new Form.Builder("formkey3")
            .withLanguage(languageCode)
            .build();
        Form enuForm4 = new Form.Builder("formkey4")
            .withLanguage(languageCode)
            .build();
        Form esnForm = new Form.Builder("formkey5")
            .withLanguage("ESN")
            .build();

        Segment segment = new Segment("segmentKey", Algorithm.FIXED_FORM);
        segment.setForms(Arrays.asList(enuForm1, enuForm2, enuForm3, enuForm4, esnForm));

        Optional<Form> maybeRetForm1A = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetForm1A).isPresent();
        Form retForm1 = maybeRetForm1A.get();
        assertThat(retForm1.getKey()).isEqualTo(enuForm1.getKey());

        Optional<Form> maybeRetForm2A = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetForm2A).isPresent();
        Form retForm2A = maybeRetForm2A.get();
        assertThat(retForm2A.getKey()).isEqualTo(enuForm2.getKey());

        Optional<Form> maybeRetForm3A = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetForm3A).isPresent();
        Form retForm3A = maybeRetForm3A.get();
        assertThat(retForm3A.getKey()).isEqualTo(enuForm3.getKey());

        Optional<Form> maybeRetForm4A = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetForm4A).isPresent();
        Form retForm4A = maybeRetForm4A.get();
        assertThat(retForm4A.getKey()).isEqualTo(enuForm4.getKey());

        Optional<Form> maybeRetForm1B = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetForm1B).isPresent();
        Form retForm1B = maybeRetForm1B.get();
        assertThat(retForm1B.getKey()).isEqualTo(enuForm1.getKey());

        Optional<Form> maybeRetForm2B = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetForm2B).isPresent();
        Form retForm2B = maybeRetForm2B.get();
        assertThat(retForm2B.getKey()).isEqualTo(enuForm2.getKey());

        Optional<Form> maybeRetForm3B = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetForm3B).isPresent();
        Form retForm3B = maybeRetForm3B.get();
        assertThat(retForm3B.getKey()).isEqualTo(enuForm3.getKey());

        Optional<Form> maybeRetForm4B = formSelector.selectForm(segment, languageCode);
        assertThat(maybeRetForm4B).isPresent();
        Form retForm4B = maybeRetForm4B.get();
        assertThat(retForm4B.getKey()).isEqualTo(enuForm4.getKey());


    }
}
