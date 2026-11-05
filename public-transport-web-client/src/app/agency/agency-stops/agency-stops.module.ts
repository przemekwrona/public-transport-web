import {NgModule} from '@angular/core';
import {AgencyStopListComponent} from './agency-stop-list.component';
import {AgencyStopTimetableService, AgencyStopsService} from '../../generated/public-transport-api';

@NgModule({
    imports: [
        AgencyStopListComponent
    ],
    exports: [
        AgencyStopListComponent
    ],
    providers: [
        AgencyStopsService,
        AgencyStopTimetableService
    ]
})
export class AgencyStopsModule {
}
