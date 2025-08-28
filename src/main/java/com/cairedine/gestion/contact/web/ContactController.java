package com.cairedine.gestion.contact.web;

import com.cairedine.gestion.contact.dataseed.service.IContactProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactController {

    private final IContactProvider provider;

    public ContactController(IContactProvider provider) {
        this.provider = provider;
    }

    @GetMapping("/")
    public String list(Model model) {
        model.addAttribute("contacts", provider.getAll());
        model.addAttribute("pageTitle", "Mes contacts");
        return "contact/list";
    }
}
