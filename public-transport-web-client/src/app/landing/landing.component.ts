import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import moment from "moment";
import {GoogleAnalyticsService} from "../google-analytics.service";
import {faPhone, IconDefinition} from "@fortawesome/free-solid-svg-icons";

@Component({
    selector: 'app-landing',
    templateUrl: './landing.component.html',
    styleUrl: './landing.component.scss',
    standalone: false
})
export class LandingComponent implements OnInit {

    public now: moment.Moment = moment();
    public faPhone: IconDefinition = faPhone;

    public NEW_LINE: string = '%0D%0A';

    public title: string = 'Zapytanie dotyczące przygotowania danych do Google Maps';
    public message: string = 'Dzień dobry,' + this.NEW_LINE + this.NEW_LINE +
        'trafiłem/am na Państwa stronę i jestem zainteresowany/a usługami związanymi z przygotowaniem danych do udostępnienia w Google Maps.' + this.NEW_LINE +
        this.NEW_LINE +
        'Chciałbym/Chciałabym dowiedzieć się więcej na temat:' + this.NEW_LINE +
        '– Zakresu usług (np. opracowania danych, integracji z API, wsparcia technicznego)' + this.NEW_LINE +
        '– Procesu współpracy i wyceny' + this.NEW_LINE +
        '– Terminów realizacji' + this.NEW_LINE +
        this.NEW_LINE +
        'Proszę o kontakt, chętnie przedstawię więcej szczegółów.' + this.NEW_LINE +
        'Z góry dziękuję za odpowiedź.' + this.NEW_LINE +
        this.NEW_LINE +
        'Pozdrawiam serdecznie,' + this.NEW_LINE +
        '[Twoje imię i nazwisko]' + this.NEW_LINE +
        '[Opcjonalnie: nazwa firmy / stanowisko]' + this.NEW_LINE +
        '[Telefon – opcjonalnie]';

    constructor(private _router: Router, private googleAnalyticsService: GoogleAnalyticsService) {
    }

    ngOnInit(): void {
        this.googleAnalyticsService.pageView('landing');
    }

    public scrollToHeader(): void {
        this.scrollToElement("#header");
    }

    public scrollToProduct(): void {
        this.scrollToElement("#product");
    }

    public scrollToAbout(): void {
        this.scrollToElement("#about");
    }

    public scrollToElement(elementId: string): void {
        const element = document.querySelector(elementId);
        if (element) element.scrollIntoView({behavior: 'smooth', block: 'start'});
    }

    public navigateToLogin(): void {
        this._router.navigate(['/signin']).then(() => {
        });
    }

    public year(): string {
        return this.now.format('yyyy');
    }

}
