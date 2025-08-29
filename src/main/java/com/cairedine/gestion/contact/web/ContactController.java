package com.cairedine.gestion.contact.web;

import com.cairedine.gestion.contact.repository.IContactRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactController {

    private final IContactRepository provider;

    public ContactController(IContactRepository provider) {
        this.provider = provider;
    }

    @GetMapping("/")
    public String list(Model model) {
        model.addAttribute("contacts", provider.findAll());
        model.addAttribute("pageTitle", "Mes contacts");
        return "contact/list";
    }


}
