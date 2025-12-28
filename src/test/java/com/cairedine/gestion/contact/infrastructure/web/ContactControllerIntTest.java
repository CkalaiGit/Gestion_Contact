package com.cairedine.gestion.contact.infrastructure.web;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.domain.entity.DBUser;
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

import java.util.List;
import java.util.Objects;

@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerIntTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    IContactService contactService;

    @Test
    void showUserContactsPage_should_renderContactList_when_contactsAreAvailable() throws Exception {
        // 1. Préparation des données simulées
        List<Contact> contacts = List.of(
                new Contact(1L, "Durand", "Alice", "alice@example.com", "0601020304"),
                new Contact(2L, "Martin", "Bob", "bob@example.com", "0605060708")
        );

        String testSub = "110736165454351850927";
        Page<@NonNull Contact> page = new PageImpl<>(contacts, PageRequest.of(0, 10), 2);

        // 2. Création du DBUser (Principal) avec le sub attendu
        DBUser mockUser = DBUser.builder()
                .sub(testSub)
                .username("Cairedine")
                .role("USER")
                .build();

        // On mocke le service.
        // Note : si votre méthode findPageForUser prend désormais l'objet DBUser, remplacez testSub par eq(mockUser)
        given(contactService.findPageForUser(eq(testSub), eq(null), eq(0), eq(10)))
                .willReturn(page);

        // 3. Exécution de la requête avec l'authentification DBUser
        MvcResult result = mvc.perform(get("/contacts")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(view().name("contact/list"))
                .andReturn();

        // 4. Analyse du HTML avec Jsoup
        String html = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);

        // Vérification du contenu
        Element h1 = doc.selectFirst("h1.h2");
        assertNotNull(h1);
        assertEquals("Mes contacts", h1.text());

        // Vérifie le tableau des contacts (2 lignes attendues)
        Elements rows = doc.select("table tbody tr");
        assertEquals(2, rows.size());

        // Vérifie le contenu du premier contact (Alice Durand)
        String firstRowText = Objects.requireNonNull(rows.first()).text();
        assertTrue(firstRowText.contains("Alice"));
        assertTrue(firstRowText.contains("Durand"));
        assertTrue(firstRowText.contains("alice@example.com"));

        // Vérifie la présence de la pagination
        assertNotNull(doc.selectFirst("nav ul.pagination"));
    }

    @Test
    void showUserContactsPage_should_renderEmptyListMessage_when_contactListIsEmpty() throws Exception {
        Page<@NonNull Contact> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        DBUser mockUser = DBUser.builder()
                .sub("12345")
                .username("Cairedine")
                .role("USER")
                .build();

        // 3. Mock du service
        // Note : On utilise any() ou eq(mockUser.getSub()) selon la signature de votre méthode
        given(contactService.findPageForUser(
                any(String.class), // ou eq(mockUser.getSub())
                eq(null),
                eq(0),
                eq(10)
        )).willReturn(emptyPage);

        // 4. Exécution avec le bon type d'objet Principal (DBUser)
        MvcResult result = mvc.perform(get("/contacts")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                )
                        )))
                .andExpect(status().isOk())
                .andExpect(view().name("contact/list"))
                .andReturn();

        // 5. Analyse HTML
        String html = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);

        // On utilise un sélecteur plus robuste pour trouver le message "Aucun contact"
        Element emptyMessage = doc.select("table tbody tr td").first();

        assertNotNull(emptyMessage, "La cellule contenant le message vide est introuvable");
        assertEquals("Aucun contact", emptyMessage.text().trim());
    }

    @Test
    void showCreateContactForm_should_renderTheForm_when_userAccessesCreatePage() throws Exception {

        DBUser mockUser = DBUser.builder()
                .sub("12345")
                .username("Cairedine")
                .role("USER")
                .build();

        MvcResult result = mvc.perform(get("/contacts/new")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
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
        // 1. Préparation des données
        Contact contact = new Contact(null, "Durand", "Alice", "alice@example.com", "0601020304");

        // On crée un DBUser mocké (ce que le contrôleur attend via @AuthenticationPrincipal)
        DBUser mockUser = DBUser.builder()
                .sub("google-123")
                .username("Alice Durand")
                .role("USER")
                .build();

        // On définit le comportement du service (il accepte un DBUser et un Contact)
        doNothing().when(contactService).createForUser(any(DBUser.class), any(Contact.class));

        // 2. Exécution de la requête
        mvc.perform(post("/contacts")
                        .flashAttr("contact", contact)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        // On injecte le DBUser comme principal
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                ))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attribute("msg", "Contact créé avec succès"));

        // 3. Vérification avec l'objet DBUser
        // On vérifie que le service a été appelé avec notre mockUser
        verify(contactService, times(1)).createForUser(eq(mockUser), any(Contact.class));
    }

    @Test
    void createContact_shouldReturnFormAndAddError_whenEmailAlreadyExists() throws Exception {
        // 1. Préparation des données
        Contact contact = new Contact(null, "Durand", "Alice", "alice@example.com", "0601020304");

        // Création du Principal attendu (DBUser)
        DBUser mockUser = DBUser.builder()
                .sub("google-123")
                .username("Alice Durand")
                .role("USER")
                .build();

        // 2. Simulation de l'exception métier
        // On utilise any(DBUser.class) car le service accepte désormais l'objet utilisateur
        doThrow(new EmailAlreadyExistsException("Cet email existe déjà"))
                .when(contactService).createForUser(any(DBUser.class), any(Contact.class));

        // 3. Exécution de la requête
        MvcResult result = mvc.perform(post("/contacts")
                        .param("firstName", contact.getFirstName())
                        .param("lastName", contact.getLastName())
                        .param("email", contact.getEmail())
                        .param("phone", contact.getPhone())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                ))))
                .andExpect(status().isOk())
                .andExpect(view().name("contact/form")) // Vérifie qu'on reste sur le formulaire
                .andReturn();

        // 4. Vérification du rendu HTML
        String html = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);

        // Vérifie que Spring a bien réinjecté l'erreur dans le BindingResult et le HTML
        Element emailError = doc.selectFirst(".invalid-feedback");
        assertNotNull(emailError, "Le message d'erreur .invalid-feedback devrait être présent");
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

        DBUser mockUser = DBUser.builder()
                .sub("google-123")
                .username("Alice Durand")
                .role("USER")
                .build();

        given(contactService.findByIdForUser(anyString(), anyLong(), anyBoolean()))
                .willReturn(contact);

        mvc.perform(get("/contacts/1/edit").with(SecurityMockMvcRequestPostProcessors.authentication(
                        new UsernamePasswordAuthenticationToken(
                                mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
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

        DBUser mockUser = DBUser.builder()
                .sub("google-123")
                .username("Alice Durand")
                .role("USER")
                .build();

        doNothing().when(contactService).updateForUser(anyString(), anyLong(), any(Contact.class));

        mvc.perform(post("/contacts/1")
                        .flashAttr("contact", contact)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                ))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attribute("msg", "Contact mis à jour"));

        verify(contactService).updateForUser(eq("google-123"), eq(1L), any(Contact.class));

    }

    @Test
    void deleteContact_shouldRedirect_whenAdminDeletesContact() throws Exception {
        doNothing().when(contactService).deleteById(1L);
        DBUser mockUser = DBUser.builder()
                .sub("google-123")
                .username("Alice Durand")
                .role("USER")
                .build();
        mvc.perform(delete("/contacts/1")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                                ))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"));

        verify(contactService, times(1)).deleteById(1L);
    }

}