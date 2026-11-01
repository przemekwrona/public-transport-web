import {ChangeDetectionStrategy, Component, DestroyRef, HostBinding, ViewEncapsulation, inject} from '@angular/core';
import {NavigationEnd, Router, RouterLink, RouterLinkActive} from '@angular/router';
import {filter} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {TranslocoPipe} from '@jsverse/transloco';
import {SidebarModule} from 'primeng/sidebar';
import {Building} from '@primeicons/angular/building';
import {Users} from '@primeicons/angular/users';
import {User} from '@primeicons/angular/user';
import {Map} from '@primeicons/angular/map';
import {ChevronDown} from '@primeicons/angular/chevron-down';
import {Directions} from '@primeicons/angular/directions';
import {Calendar} from '@primeicons/angular/calendar';
import {Clipboard} from '@primeicons/angular/clipboard';
import {Google} from '@primeicons/angular/google';
import {LoginService} from '../../auth/login.service';
import {AgencyStorageService} from '../../auth/agency-storage.service';
import {AgencySidebarService} from './agency-sidebar.service';

@Component({
    imports: [
        RouterLink,
        RouterLinkActive,
        TranslocoPipe,
        SidebarModule,
        Building,
        Users,
        User,
        Map,
        ChevronDown,
        Directions,
        Calendar,
        Clipboard,
        Google
    ],
    providers: [
        LoginService
    ],
    selector: 'app-sidebar',
    templateUrl: './sidebar.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './sidebar.component.scss',
    encapsulation: ViewEncapsulation.None
})
export class SidebarComponent {
    @HostBinding('style.display') hostDisplay = 'contents';

    constructor(
        private authService: LoginService,
        private agencyStorageService: AgencyStorageService,
        private router: Router,
        readonly sidebar: AgencySidebarService
    ) {
        this.sidebar.applyLayout();
        inject(DestroyRef).onDestroy(() => this.sidebar.clearLayout());
        this.router.events.pipe(
            filter((event): event is NavigationEnd => event instanceof NavigationEnd),
            takeUntilDestroyed()
        ).subscribe(() => this.sidebar.close());
    }

    isAdmin(): boolean {
        return this.authService.hasRoleSuperAdmin();
    }

    getAgencyName(): string {
        return this.agencyStorageService.getInstance();
    }

    onOpenChange(open: boolean): void {
        this.sidebar.setOpen(open);
    }
}
