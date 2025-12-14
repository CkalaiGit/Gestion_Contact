package com.cairedine.gestion.contact.infrastructure.web;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.domain.exception.EmailAlreadyExistsException;
import com.cairedine.gestion.contact.domain.service.IContactService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerIntTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    IContactService contactService;

    @Test
    void showUserContactsPage_should_renderContactList_when_contactsAreAvailable() throws Exception {
        // Préparation des données simulées
        List<Contact> contacts = List.of(
                new Contact(1L, "Durand", "Alice", "alice@example.com", "0601020304"),
                new Contact(2L, "Martin", "Bob", "bob@example.com", "0605060708")
        );

        Page<@NonNull Contact> page = new PageImpl<>(contacts, PageRequest.of(0, 10), 2);

        given(contactService.findPageForUser("110736165454351850927", null, 0, 10)).willReturn(page);

        // Utilisation de la méthode privée pour créer un OIDC user factice
        OidcUser oidcUser = stubOidcUser();

        // Injection du principal OIDC dans MockMvc
        MvcResult result = mvc.perform(get("/contacts")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        oidcUser, "N/A", oidcUser.getAuthorities()
                                )
                        )))
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
    void showUserContactsPage_should_renderEmptyListMessage_when_contactListIsEmpty() throws Exception {
        Page<@NonNull Contact> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        given(contactService.findPageForUser(
                any(String.class),
                eq(null),
                eq(0),
                eq(10)
        )).willReturn(emptyPage);

        OidcUser oidcUser = stubOidcUser();

        MvcResult result = mvc.perform(get("/contacts")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        oidcUser, "N/A", oidcUser.getAuthorities()
                                )
                        )))
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
    void showCreateContactForm_should_renderTheForm_when_userAccessesCreatePage() throws Exception {
        OidcUser oidcUser = stubOidcUser();
        MvcResult result = mvc.perform(get("/contacts/new")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        oidcUser, "N/A", oidcUser.getAuthorities()
                                )
                        )))
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
    void createContact_shouldRedirectToContacts_whenContactIsValid() throws Exception {
        Contact contact = new Contact(null, "Durand", "Alice", "alice@example.com", "0601020304");

        OidcUser oidcUser = stubOidcUser();
        String expectedSub = oidcUser.getName();
        doNothing().when(contactService).createForUser(any(String.class), any(Contact.class));

        mvc.perform(post("/contacts")
                        .flashAttr("contact", contact)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        oidcUser, "N/A", oidcUser.getAuthorities()
                                ))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attribute("msg", "Contact créé avec succès"));

        verify(contactService, times(1)).createForUser(expectedSub, contact);
    }

    @Test
    void createContact_shouldReturnFormAndAddError_whenEmailAlreadyExists() throws Exception {
        Contact contact = new Contact(null, "Durand", "Alice", "alice@example.com", "0601020304");
        OidcUser oidcUser = stubOidcUser();

        // Simule une exception métier
        doThrow(new EmailAlreadyExistsException("Cet email existe déjà"))
                .when(contactService).createForUser(any(String.class), any(Contact.class));

        MvcResult result = mvc.perform(post("/contacts")
                        .param("firstName", contact.getFirstName())
                        .param("lastName", contact.getLastName())
                        .param("email", contact.getEmail())
                        .param("phone", contact.getPhone())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        oidcUser, "N/A", oidcUser.getAuthorities()
                                ))))
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
    void showEditForm_shouldReturnEditForm_whenContactIsOwnedByUser() throws Exception {
        Contact contact = Contact.builder()
                .id(1L)
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@acme.com")
                .phone("0601010101")
                .build();

        OidcUser oidcUser = stubOidcUser(); // renvoie sub = "110736165454351850927"

        given(contactService.findByIdForUser("110736165454351850927", 1L, true))
                .willReturn(contact);

        mvc.perform(get("/contacts/1/edit").with(SecurityMockMvcRequestPostProcessors.authentication(
                        new UsernamePasswordAuthenticationToken(
                                oidcUser, "N/A", oidcUser.getAuthorities()
                        ))))
                .andExpect(status().isOk())
                .andExpect(view().name("contact/form"))
                .andExpect(model().attribute("contact", contact));
    }


    @Test
    void shouldUpdateContactSuccessfully_WhenFormIsValid() throws Exception {
        Contact contact = Contact.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Durand")
                .email("Alice.Durand@gmail.com")
                .phone("0601020304")
                .build();

        OidcUser oidcUser = stubOidcUser();

        doNothing().when(contactService).updateForUser("alice", 1L, contact);

        mvc.perform(post("/contacts/1")
                        .flashAttr("contact", contact)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        oidcUser, "N/A", oidcUser.getAuthorities()
                                ))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attribute("msg", "Contact mis à jour"));

        verify(contactService).updateForUser(eq("110736165454351850927"), eq(1L), any(Contact.class));

    }

    @Test
    void deleteContact_shouldRedirect_whenAdminDeletesContact() throws Exception {
        doNothing().when(contactService).deleteById(1L);

        mvc.perform(delete("/contacts/1")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(stubOidcUser(), "N/A", stubOidcUser().getAuthorities()))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"));

        verify(contactService, times(1)).deleteById(1L);
    }

    private OidcUser stubOidcUser() {
        OidcIdToken idToken = new OidcIdToken(
                "fake-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of(
                        "sub", "110736165454351850927",
                        "email", "alice@example.com",
                        "name", "Alice Durand"
                )
        );

        return new DefaultOidcUser(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                idToken
        );
    }

}