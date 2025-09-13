package com.cairedine.gestion.contact.infrastructure.web;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.domain.exception.EmailAlreadyExistsException;
import com.cairedine.gestion.contact.domain.service.IContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    private final IContactService iContactService;

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String query,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       Model model) {

        if (size != 5 && size != 10 && size != 15) size = 10;
        if (page < 0) page = 0;

        Page<Contact> contactsPage = iContactService.findPage(query, page, size);

        model.addAttribute("contactsPage", contactsPage);
        model.addAttribute("contacts", contactsPage.getContent());
        model.addAttribute("pageTitle", "Mes contacts");
        model.addAttribute("size", size);
        model.addAttribute("q", query);
        model.addAttribute("pageSizes", List.of(5, 10, 15));

        return "contact/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("pageTitle", "Nouveau contact");
        model.addAttribute("contact", new Contact()); // objet vide pour binding
        return "contact/form";
    }

    @PostMapping
    public String createContact(
            @Valid @ModelAttribute("contact") Contact contact,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Nouveau contact");
            return "contact/form";
        }

        try {
            iContactService.create(contact);
        } catch (EmailAlreadyExistsException e) {
            bindingResult.rejectValue("email", "error.contact", e.getMessage());
            model.addAttribute("pageTitle", "Nouveau contact");
            return "contact/form";
        }

        redirectAttributes.addFlashAttribute("msg", "Contact créé avec succès");
        return "redirect:/contacts";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Contact contact = iContactService.findById(id);
        model.addAttribute("pageTitle", "Éditer le contact");
        model.addAttribute("contact", contact);
        return "contact/form";
    }

    @PostMapping("/{id}")
    public String updateContact(@PathVariable Long id,
                                @Valid @ModelAttribute("contact") Contact contact,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Éditer le contact");
            return "contact/form";
        }
        try {
            iContactService.update(id, contact); // l’id de l’URL fait foi
        } catch (EmailAlreadyExistsException e) {
            bindingResult.rejectValue("email", "error.contact", e.getMessage());
            model.addAttribute("pageTitle", "Éditer le contact");
            return "contact/form";
        }
        redirectAttributes.addFlashAttribute("msg", "Contact mis à jour");
        return "redirect:/contacts";
    }

    @DeleteMapping("/{id}")
    public String deleteContact(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        iContactService.deleteById(id);
        redirectAttributes.addFlashAttribute("msg", "Contact mis à jour");
        return "redirect:/contacts";
    }


}

