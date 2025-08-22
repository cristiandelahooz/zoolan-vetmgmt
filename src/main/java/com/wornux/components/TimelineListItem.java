package com.wornux.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.theme.lumo.LumoUtility.*;
import org.vaadin.lineawesome.LineAwesomeIcon;

public class TimelineListItem extends ListItem {

    private Div icon;
    private Html content;
    private Avatar avatar;
    private Span author;
    private Span activity;
    private Span time;

    private TimelineListItem(LineAwesomeIcon icon, String iconBackgroundColor, String iconTextColor) {
        addClassNames("timeline");
        setIcon(icon, iconBackgroundColor, iconTextColor);
    }

    public TimelineListItem(LineAwesomeIcon icon, String iconBackgroundColor, String iconTextColor, String content) {
        this(icon, iconBackgroundColor, iconTextColor, content, "");
    }

    public TimelineListItem(LineAwesomeIcon icon, String iconBackgroundColor, String iconTextColor, String content,
            String time) {
        this(icon, iconBackgroundColor, iconTextColor);
        setContent(content);

        this.time = new Span(time);
        this.time.addClassNames(FontSize.SMALL, TextColor.SECONDARY);

        setPrefix(this.icon);
        setPrimary(this.content);
        setSuffix(this.time);
    }

    public TimelineListItem(LineAwesomeIcon icon, String iconBackgroundColor, String iconTextColor, String author,
            String activity, String time) {
        this(icon, iconBackgroundColor, iconTextColor, author, new Text(" " + activity + " "), time);
    }

    public TimelineListItem(LineAwesomeIcon icon, String iconBackgroundColor, String iconTextColor, String author,
            Component activity, String time) {
        this(icon, iconBackgroundColor, iconTextColor);

        this.avatar = new Avatar(author);
        this.avatar.addThemeVariants(AvatarVariant.LUMO_XSMALL);

        this.author = new Span(author);
        this.author.addClassNames(FontWeight.SEMIBOLD, TextColor.BODY);

        this.activity = new Span(activity);
        this.activity.addClassNames(TextColor.SECONDARY);

        this.time = new Span(time);
        this.time.addClassNames(FontSize.SMALL, TextColor.SECONDARY);

        setPrefix(this.icon);
        setPrimary(this.avatar, new Span(this.author, this.activity));
        setSuffix(this.time);
    }

    public void setIcon(LineAwesomeIcon icon, String backgroundColor, String color) {
        SvgIcon i = icon.create();
        i.addClassNames(IconSize.SMALL);

        this.icon = new Div(i);
        this.icon.addClassNames(Display.FLEX, backgroundColor, Border.ALL, BorderColor.CONTRAST_30, "rounded-full",
                Height.MEDIUM, color, Width.MEDIUM, AlignItems.CENTER, BoxSizing.BORDER, JustifyContent.CENTER);
    }

    public void setContent(String content) {
        this.content = new Html("<span class='" + TextColor.SECONDARY + "'>" + content.replace("<b>",
                "<span class='" + FontWeight.SEMIBOLD + " " + TextColor.BODY + "'>").replace("</b>",
                        "</span>") + "</span>");
    }

    public void setAvatarImage(String url) {
        this.avatar.setImage(url);
    }

    public void setAuthor(String author) {
        this.avatar.setName(author);
        this.author.setText(author);
    }

    public void setActivity(String activity) {
        this.activity.setText(activity);
    }

    public void setActivity(Component... components) {
        this.activity.removeAll();
        this.activity.add(components);
    }

    public void setTime(String time) {
        this.time.setText(time);
    }
}
