package com.cairedine.gestion.contact.infrastructure.web;

import com.cairedine.gestion.contact.domain.service.IContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ContactController {

    private final IContactService iContactService;

    @GetMapping("/contacts")
    public String list(Model model) {
        model.addAttribute("contacts", iContactService.findAllSorted());
        model.addAttribute("pageTitle", "Mes contacts");
        return "contact/list";
    }


}
