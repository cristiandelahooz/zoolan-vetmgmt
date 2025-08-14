package com.wornux.views.transactions;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route("invoicetmp")
@PermitAll
public class InvoiceViewTmp extends VerticalLayout {

  public InvoiceViewTmp() {
    setWidthFull();
    setPadding(true);
    setSpacing(true);

    add(
        buildHeader(),
        buildCreateSection(),
        buildSendSection(),
        buildTermsSection(),
        buildManagePaymentSection());
  }

  private Component buildHeader() {
    HorizontalLayout header = new HorizontalLayout();
    header.setWidthFull();
    header.setJustifyContentMode(JustifyContentMode.BETWEEN);

    Div left = new Div();
    H2 invoiceTitle = new H2("Invoice #101");

    Span status = new Span("Paid");
    status
        .getStyle()
        .set("background", "#d1f7e2")
        .set("color", "#1a7f37")
        .set("padding", "4px 12px")
        .set("border-radius", "6px")
        .set("font-weight", "bold");

    left.add(invoiceTitle, status);
    left.getStyle().set("display", "flex").set("align-items", "center").set("gap", "1em");

    HorizontalLayout actions = new HorizontalLayout();
    actions.add(
        new Button("Online Payments ● ON"),
        new Button("More actions"),
        new Button("Create another invoice"));

    header.add(left, actions);
    return header;
  }

  private Component buildCreateSection() {
    Details details =
        new Details("Create", new Span("Created on: September 25, 2024 at 9:14 PM AST"));
    Button edit = new Button("Edit invoice");
    HorizontalLayout layout = new HorizontalLayout(details, edit);
    layout.setAlignItems(Alignment.CENTER);
    layout.setWidthFull();
    return layout;
  }

  private Component buildSendSection() {
    VerticalLayout sendInfo = new VerticalLayout();
    sendInfo.setSpacing(false);
    sendInfo.setPadding(false);
    sendInfo.add(
        new Span("Last sent: with Wave on October 7, 2024."),
        new Span("Last viewed by customer: on October 11, 2024 at 7:30 PM AST"));

    Button resend = new Button("Resend invoice");
    resend.getStyle().set("margin-top", "8px");

    return new VerticalLayout(new H4("Send"), sendInfo, resend);
  }

  private Component buildTermsSection() {
    HorizontalLayout layout = new HorizontalLayout();
    layout.setWidthFull();
    Span label =
        new Span("Require customer to agree to terms of service before paying by credit card");
    label.getStyle().set("color", "#888");

    Span toggle = new Span("OFF");
    toggle
        .getStyle()
        .set("background", "#eee")
        .set("padding", "4px 10px")
        .set("border-radius", "12px");

    Button edit = new Button("Edit");

    layout.add(toggle, label, edit);
    layout.setAlignItems(Alignment.CENTER);
    layout.setSpacing(true);
    return layout;
  }

  private Component buildManagePaymentSection() {
    VerticalLayout layout = new VerticalLayout();
    layout.setPadding(false);
    layout.setSpacing(false);

    layout.add(
        new Span("Amount due: $0.00 — Status: Your invoice is paid in full"),
        new Span("October 10, 2024 - A payment for $337.50 was made using AMEX •••• 1014."));

    HorizontalLayout links = new HorizontalLayout();
    links.add(
        new Anchor("#", "Send a receipt"),
        new Anchor("#", "Edit payment"),
        new Anchor("#", "View details"));

    layout.add(links);
    return new VerticalLayout(new H4("Manage Payments"), layout);
  }
}
