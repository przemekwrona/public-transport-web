import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import moment from "moment";
import {GoogleAnalyticsService} from "../google-analytics.service";

@Component({
    selector: 'app-landing',
    templateUrl: './landing.component.html',
    styleUrl: './landing.component.scss'
})
export class LandingComponent implements OnInit {

    public now: moment.Moment = moment()

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
