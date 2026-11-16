import {DatePipe} from '@angular/common';
import {ChangeDetectionStrategy, Component} from '@angular/core';
import {TranslocoPipe, TranslocoService} from '@jsverse/transloco';
import {TableLazyLoadEvent, TableModule} from 'primeng/table';
import {LoginEventItem, LoginEventsService} from '../../generated/public-transport-api';
import {NotificationService} from '../../shared/notification.service';

@Component({
    selector: 'app-login-events',
    imports: [TableModule, TranslocoPipe, DatePipe],
    templateUrl: './login-events.component.html',
    changeDetection: ChangeDetectionStrategy.Eager
})
export class LoginEventsComponent {

    events: LoginEventItem[] = [];
    totalRecords = 0;
    loading = false;
    readonly rows = 20;

    constructor(
        private loginEventsService: LoginEventsService,
        private notificationService: NotificationService,
        private transloco: TranslocoService
    ) {
    }

    load(event: TableLazyLoadEvent): void {
        const rows = event.rows && event.rows > 0 ? event.rows : this.rows;
        const first = event.first ?? 0;
        this.loading = true;
        this.loginEventsService.getLoginEvents(Math.floor(first / rows), rows).subscribe({
            next: (response) => {
                this.events = response.events ?? [];
                this.totalRecords = response.totalElements ?? 0;
                this.loading = false;
            },
            error: () => {
                this.events = [];
                this.loading = false;
                this.notificationService.showError(this.transloco.translate('admin.logins.loadFailed'));
            }
        });
    }

}
