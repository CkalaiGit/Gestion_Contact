package com.cairedine.gestion.contact.infrastructure.web;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.domain.entity.DBUser;
import com.cairedine.gestion.contact.domain.exception.EmailAlreadyExistsException;
import com.cairedine.gestion.contact.domain.service.IContactService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("/contacts")
@SuppressWarnings("unused")
public class ContactController {

    public static final String PAGE_TITLE = "pageTitle";
    public static final String CONTACT_FORM = "contact/form";
    public static final String REDIRECT_CONTACTS = "redirect:/contacts";
    public static final String EDITER_LE_CONTACT = "Éditer le contact";
    public static final String NOUVEAU_CONTACT = "Nouveau contact";
    private final IContactService iContactService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String displayContacts(@RequestParam(value = "q", required = false) String query,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               @AuthenticationPrincipal DBUser user,
                               Model model) {

        // Validation des paramètres
        if (!List.of(5, 10, 15).contains(size)) {
            size = 10;
        }
        if (page < 0) {
            page = 0;
        }

        Page<@NonNull Contact> contactsPage = iContactService.findPageForUser(user.getSub(), query, page, size);

        // Ajout des attributs au modèle
        model.addAttribute("contactsPage", contactsPage);
        model.addAttribute("contacts", contactsPage.getContent());
        model.addAttribute(PAGE_TITLE, "Mes contacts");
        model.addAttribute("size", size);
        model.addAttribute("q", query);
        model.addAttribute("pageSizes", List.of(5, 10, 15));

        return "contact/list";
    }


    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute(PAGE_TITLE, NOUVEAU_CONTACT);
        model.addAttribute("contact", new Contact()); // objet vide pour binding
        return CONTACT_FORM;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String createContact(
            @AuthenticationPrincipal DBUser user,
            @Valid @ModelAttribute("contact") Contact contact,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute(PAGE_TITLE, NOUVEAU_CONTACT);
            return CONTACT_FORM;
        }

        try {
            iContactService.createForUser(user, contact);
        } catch (EmailAlreadyExistsException e) {
            bindingResult.rejectValue("email", "error.contact", e.getMessage());
            model.addAttribute(PAGE_TITLE, NOUVEAU_CONTACT);
            return CONTACT_FORM;
        }

        redirectAttributes.addFlashAttribute("msg", "Contact créé avec succès");
        return REDIRECT_CONTACTS;
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal OidcUser user,
                               Model model) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));


        Contact contact = iContactService.findByIdForUser(user.getSubject(), id, isAdmin);

        model.addAttribute(PAGE_TITLE, EDITER_LE_CONTACT);
        model.addAttribute("contact", contact);
        return CONTACT_FORM;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String updateContact(@PathVariable Long id,
                                @Valid @ModelAttribute("contact") Contact contact,
                                BindingResult bindingResult,
                                Model model,
                                @AuthenticationPrincipal OidcUser user,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(PAGE_TITLE, EDITER_LE_CONTACT);
            return CONTACT_FORM;
        }



        try {
            iContactService.updateForUser(user.getSubject(), id, contact);
        } catch (EmailAlreadyExistsException e) {
            bindingResult.rejectValue("email", "error.contact", e.getMessage());
            model.addAttribute(PAGE_TITLE, EDITER_LE_CONTACT);
            return CONTACT_FORM;
        }

        redirectAttributes.addFlashAttribute("msg", "Contact mis à jour");
        return REDIRECT_CONTACTS;
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String deleteContact(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        iContactService.deleteById(id);
        redirectAttributes.addFlashAttribute("msg", "Contact mis à jour");
        return REDIRECT_CONTACTS;
    }


}

