import {Component, HostBinding} from '@angular/core';
import {LoginService} from "../../auth/login.service";
import {TranslocoPipe} from "@jsverse/transloco";
import {Router} from "@angular/router";

@Component({
    standalone: true,
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrl: './header.component.scss',
    imports: [TranslocoPipe],
    providers: [LoginService]
})
export class HeaderComponent {
    @HostBinding('class') hostClass = 'header fixed top-0 z-10 left-0 right-0 flex items-stretch shrink-0 bg-[#fefefe] dark:bg-coal-500 shadow-sm dark:border-b dark:border-b-coal-100';
    @HostBinding('attr.role') hostRole = 'banner';
    @HostBinding('attr.data-sticky') dataSticky = 'true';
    @HostBinding('attr.data-sticky-name') dataStickyName = 'header';
    @HostBinding('id') hostId = 'header';

    constructor(private authService: LoginService, private router: Router) {
    }

    public logout(): void {
        this.authService.logout();
        this.router.navigate(['/signin']).then(() => {});
    }
}
