package com.cairedine.gestion.contact.web;

import com.cairedine.gestion.contact.repository.IContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ContactController {

    private final IContactRepository provider;

    @GetMapping("/contacts")
    public String list(Model model) {
        model.addAttribute("contacts", provider.findAll());
        model.addAttribute("pageTitle", "Mes contacts");
        return "contact/list";
    }


}
