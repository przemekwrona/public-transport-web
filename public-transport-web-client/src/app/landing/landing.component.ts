import {Component} from '@angular/core';
import {Router} from "@angular/router";

@Component({
    selector: 'app-landing',
    templateUrl: './landing.component.html',
    styleUrl: './landing.component.scss'
})
export class LandingComponent {

    constructor(private _router: Router) {
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

}
