package com.cairedine.gestion.contact.infrastructure.web;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.domain.exception.EmailAlreadyExistsException;
import com.cairedine.gestion.contact.domain.service.IContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    private IContactService contactService;

    @Test
    void shouldRenderContactListWithContacts() throws Exception {
        // Préparation des données simulées
        List<Contact> contacts = List.of(
                new Contact(1L, "Durand", "Alice", "alice@example.com", "0601020304"),
                new Contact(2L, "Martin", "Bob", "bob@example.com", "0605060708")
        );
        Page<Contact> page = new PageImpl<>(contacts, PageRequest.of(0, 10), 2);

        when(contactService.findPage(null, 0, 10)).thenReturn(page);

        // Appel du contrôleur
        MvcResult result = mvc.perform(get("/contacts"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact/list"))
                .andReturn();

        // Analyse du HTML avec Jsoup
        String html = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);

        // Vérifie le titre
        Element h1 = doc.selectFirst("h1.h2");
        assertNotNull(h1);
        assertEquals("Mes contacts", h1.text());

        // Vérifie le formulaire de recherche
        Element searchInput = doc.selectFirst("input[name=q]");
        assertNotNull(searchInput);
        assertEquals("Rechercher...", searchInput.attr("placeholder"));

        // Vérifie le tableau des contacts
        Elements rows = doc.select("table tbody tr");
        assertEquals(2, rows.size());

        // Vérifie le contenu du premier contact
        Element firstRow = rows.getFirst();
        assertTrue(firstRow.text().contains("Alice Durand"));
        assertTrue(firstRow.text().contains("alice@example.com"));
        assertTrue(firstRow.text().contains("0601020304"));

        // Vérifie la pagination
        Element pagination = doc.selectFirst("nav ul.pagination");
        assertNotNull(pagination);
        Elements pageLinks = pagination.select("li.page-item");
        assertFalse(pageLinks.isEmpty());
    }

    @Test
    void shouldRenderEmptyContactListMessage() throws Exception {
        Page<Contact> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(contactService.findPage(null, 0, 10)).thenReturn(emptyPage);

        MvcResult result = mvc.perform(get("/contacts"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact/list"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);

        Element emptyMessage = doc.selectFirst("table tbody tr td[colspan=4]");
        assertNotNull(emptyMessage);
        assertEquals("Aucun contact", emptyMessage.text());
    }

    @Test
    void shouldRenderCreateContactForm() throws Exception {
        MvcResult result = mvc.perform(get("/contacts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact/form"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);

        // Vérifie le titre
        Element h1 = doc.selectFirst("h1");
        assertNotNull(h1);
        assertEquals("Nouveau contact", h1.text());

        // Vérifie le formulaire
        Element form = doc.selectFirst("form");
        assertNotNull(form);
        assertEquals("post", form.attr("method"));
        assertTrue(form.attr("action").contains("/contacts"));

        // Vérifie les champs du formulaire
        assertNotNull(doc.selectFirst("input[id=firstName]"));
        assertNotNull(doc.selectFirst("input[id=lastName]"));
        assertNotNull(doc.selectFirst("input[id=email]"));
        assertNotNull(doc.selectFirst("input[id=phone]"));

        // Vérifie les boutons
        Element submitButton = doc.selectFirst("button[type=submit]");
        assertNotNull(submitButton);
        assertEquals("Enregistrer", submitButton.text());

        Element cancelLink = doc.selectFirst("a.btn-secondary");
        assertNotNull(cancelLink);
        assertTrue(cancelLink.attr("href").contains("/contacts"));
    }

    @Test
    void shouldCreateContactSuccessfully() throws Exception {
        Contact contact = new Contact(null, "Durand", "Alice", "alice@example.com", "0601020304");

        // Simule la création sans exception
        doNothing().when(contactService).create(any(Contact.class));

        mvc.perform(post("/contacts")
                        .param("firstName", contact.getFirstName())
                        .param("lastName", contact.getLastName())
                        .param("email", contact.getEmail())
                        .param("phone", contact.getPhone())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attribute("msg", "Contact créé avec succès"));
    }

    @Test
    void shouldReturnFormWhenEmailAlreadyExists() throws Exception {
        Contact contact = new Contact(null, "Durand", "Alice", "alice@example.com", "0601020304");

        // Simule une exception métier
        doThrow(new EmailAlreadyExistsException("Cet email existe déjà"))
                .when(contactService).create(any(Contact.class));

        MvcResult result = mvc.perform(post("/contacts")
                        .param("firstName", contact.getFirstName())
                        .param("lastName", contact.getLastName())
                        .param("email", contact.getEmail())
                        .param("phone", contact.getPhone())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("contact/form"))
                .andReturn();

        // Vérifie que le message d'erreur est affiché dans le HTML
        String html = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);
        Element emailError = doc.selectFirst(".invalid-feedback");
        assertNotNull(emailError);
        assertTrue(emailError.text().contains("Cet email existe déjà"));
    }

    @Test
    public void testDeleteContact() throws Exception {

        doNothing().when(contactService).deleteById(1L);

        mvc.perform(delete("/contacts/" + 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"));

        verify(contactService, times(1)).deleteById(1L);
    }


}