package com.cairedine.gestion.contact.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void homeControllerTest() throws Exception {

        var mvcResult = mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Bienvenue")))
                .andExpect(view().name("home/index"))
                .andReturn();
        String html = mvcResult.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);

        // Vérifie que la balise <section> existe
        Element section = doc.selectFirst("section");
        assertNotNull(section, "La balise <section> doit être présente");

        // Sélectionne tous les <h2> dans la section
        Elements h2s = section.select("h2");
        assertEquals(3, h2s.size(), "Il doit y avoir 3 titres <h2> dans la section");

        // Vérifie le contenu des <h2>
        List<String> expectedTitles = List.of(
                "Structure du projet",
                "Fonctionnalités actuelles",
                "Évolutions prévues"
        );

        for (int i = 0; i < h2s.size(); i++) {
            assertEquals(expectedTitles.get(i), h2s.get(i).text(), "Le contenu du <h2> est incorrect");
        }

    }
}