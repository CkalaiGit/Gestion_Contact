package com.cairedine.gestion.contact.infrastructure.web;

import com.cairedine.gestion.contact.domain.entity.Contact;
import com.cairedine.gestion.contact.domain.service.IContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ContactController {

    private final IContactService iContactService;

    @GetMapping("/contacts")
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


}
