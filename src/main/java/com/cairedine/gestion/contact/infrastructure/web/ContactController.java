package com.cairedine.gestion.contact.infrastructure.web;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.domain.entity.DBUser;
import com.cairedine.gestion.contact.domain.exception.EmailAlreadyExistsException;
import com.cairedine.gestion.contact.domain.service.IContactService;
import com.cairedine.gestion.contact.infrastructure.repository.IUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/contacts")
public class ContactController {

    // TODO : Injection du mauvais type dans le contrôleur Tu as utilisé @AuthenticationPrincipal User user (Spring UserDetails).
    //  Or ton appli est branchée sur OIDC, donc le principal est un OidcUser.
    //  Spring ne sait pas convertir automatiquement → user était null → NPE (user.getAuthorities()).
    //  Cause : mauvais type injecté (User au lieu de OidcUser). -> Résolu en utilisant @AuthenticationPrincipal OidcUser oidcUser en lieu de User user.

    private final IContactService iContactService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String list(@RequestParam(value = "q", required = false) String query,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       @AuthenticationPrincipal String sub,
                       Model model) {

        if (size != 5 && size != 10 && size != 15) size = 10;
        if (page < 0) page = 0;

        Page<Contact> contactsPage = iContactService.findPageForUser(sub, query, page, size);

        model.addAttribute("contactsPage", contactsPage);
        model.addAttribute("contacts", contactsPage.getContent());
        model.addAttribute("pageTitle", "Mes contacts");
        model.addAttribute("size", size);
        model.addAttribute("q", query);
        model.addAttribute("pageSizes", List.of(5, 10, 15));

        return "contact/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("pageTitle", "Nouveau contact");
        model.addAttribute("contact", new Contact()); // objet vide pour binding
        return "contact/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String createContact(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("contact") Contact contact,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Nouveau contact");
            return "contact/form";
        }

        try {
            iContactService.createForUser(user.getUsername(), contact);
        } catch (EmailAlreadyExistsException e) {
            bindingResult.rejectValue("email", "error.contact", e.getMessage());
            model.addAttribute("pageTitle", "Nouveau contact");
            return "contact/form";
        }

        redirectAttributes.addFlashAttribute("msg", "Contact créé avec succès");
        return "redirect:/contacts";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal User user,
                               Model model) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));


        Contact contact = iContactService.findByIdForUser(user.getUsername(), id, isAdmin);

        model.addAttribute("pageTitle", "Éditer le contact");
        model.addAttribute("contact", contact);
        return "contact/form";
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public String updateContact(@PathVariable Long id,
                                @Valid @ModelAttribute("contact") Contact contact,
                                BindingResult bindingResult,
                                Model model,
                                @AuthenticationPrincipal OidcUser oidcUser,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Éditer le contact");
            return "contact/form";
        }

        // Récupérer ton DBUser en base via le sub
        IUserRepository userRepository = null; // on passe par un service dans une vraie appli
        DBUser dbUser = userRepository.findBySub(oidcUser.getSubject())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));

        try {
            iContactService.updateForUser(dbUser.getSub(), id, contact);
        } catch (EmailAlreadyExistsException e) {
            bindingResult.rejectValue("email", "error.contact", e.getMessage());
            model.addAttribute("pageTitle", "Éditer le contact");
            return "contact/form";
        }

        redirectAttributes.addFlashAttribute("msg", "Contact mis à jour");
        return "redirect:/contacts";
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String deleteContact(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        iContactService.deleteById(id);
        redirectAttributes.addFlashAttribute("msg", "Contact mis à jour");
        return "redirect:/contacts";
    }


}

