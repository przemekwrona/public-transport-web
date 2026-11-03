import {NgModule} from '@angular/core';
import {AgencyStopListComponent} from './agency-stop-list.component';
import {AgencyStopsService} from '../../generated/public-transport-api';

@NgModule({
    imports: [
        AgencyStopListComponent
    ],
    exports: [
        AgencyStopListComponent
    ],
    providers: [
        AgencyStopsService
    ]
})
export class AgencyStopsModule {
}
