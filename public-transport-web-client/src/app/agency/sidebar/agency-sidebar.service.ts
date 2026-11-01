import {Injectable, signal} from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class AgencySidebarService {
    private readonly mobileQuery = '(max-width: 1023px)';
    private readonly expandedWidth = '17.5rem';
    private readonly iconWidth = '5rem';

    readonly mobile = signal(this.matchesMobile());
    readonly open = signal(!this.mobile());

    constructor() {
        this.applyLayout();
        if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
            return;
        }
        const media = window.matchMedia(this.mobileQuery);
        const apply = () => {
            const mobile = media.matches;
            this.mobile.set(mobile);
            this.open.set(!mobile);
            this.applyLayout();
        };
        apply();
        media.addEventListener('change', apply);
    }

    toggle(): void {
        this.open.update((open) => !open);
        this.applyLayout();
    }

    close(): void {
        if (!this.mobile()) {
            return;
        }
        this.open.set(false);
        this.applyLayout();
    }

    setOpen(open: boolean): void {
        this.open.set(open);
        this.applyLayout();
    }

    applyLayout(): void {
        if (typeof document === 'undefined') {
            return;
        }
        document.body.classList.add('agency-sidebar-push');
        document.body.style.setProperty('--tw-sidebar-width', this.sidebarWidth());
    }

    clearLayout(): void {
        if (typeof document === 'undefined') {
            return;
        }
        document.body.classList.remove('agency-sidebar-push', 'sidebar-collapse');
        document.body.style.removeProperty('--tw-sidebar-width');
    }

    private sidebarWidth(): string {
        if (this.mobile()) {
            return '0px';
        }
        return this.open() ? this.expandedWidth : this.iconWidth;
    }

    private matchesMobile(): boolean {
        return typeof window !== 'undefined'
            && typeof window.matchMedia === 'function'
            && window.matchMedia(this.mobileQuery).matches;
    }
}
