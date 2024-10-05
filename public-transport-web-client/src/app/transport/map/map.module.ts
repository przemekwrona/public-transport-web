import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MapComponent} from "./map.component";
import {StopService} from "../../http/stop.service";
import {DepartureService} from "../../http/departure.service";
import {DeparturesComponent} from "./departures/departures.component";
import {MatGridListModule} from "@angular/material/grid-list";
import {DepartureTimePipe} from "./departures/departure-time.pipe";
import {RouteService} from "../../http/route.service";
import {RoutesComponent} from "./routes/routes.component";
import {TimetableService} from "../../http/timetable.service";
import {TimetableComponent} from "./timetable/timetable.component";
import {TimetableHourPipe} from "./timetable/timetable-hour.pipe";
import {StopComponent} from "./stop/stop.component";
import {DepartureTimePrefixPipe} from "./departures/departure-time-prefix.pipe";
import {MatStepperModule} from "@angular/material/stepper";
import {MatButtonModule} from "@angular/material/button";
import {STEPPER_GLOBAL_OPTIONS} from "@angular/cdk/stepper";
import {TimetableContentComponent} from "./timetable-content/timetable-content.component";
import {OtpModule} from "./otp/otp.module";
import {MatIconModule} from "@angular/material/icon";
import {BrandHeaderComponent} from "./brand-header/brand-header.component";
import {OtpService} from "../../http/otp.service";
import {TimetableMinutePipe} from "./timetable/timetable-minute.pipe";
import {GbfsService} from "../../http/gbfs.service";
import {MapService} from "./service/map.service";
import {BikeMapManagerService} from "./service/bike-map-manager.service";
import {ItineraryManagerService} from "./service/itinerary-manager.service";
import {CityManagerService} from "./service/city-manager.service";


@NgModule({
    declarations: [
        MapComponent,
        StopComponent,
        DeparturesComponent,
        DepartureTimePipe,
        DepartureTimePrefixPipe,
        RoutesComponent,
        TimetableComponent,
        TimetableHourPipe,
        TimetableMinutePipe,
        TimetableContentComponent,
        BrandHeaderComponent
    ],
    exports: [
        MapComponent
    ],
    imports: [
        CommonModule,
        OtpModule,
        MatGridListModule,
        MatStepperModule,
        MatButtonModule,
        MatIconModule
    ],
    providers: [
        MapService,
        BikeMapManagerService,
        ItineraryManagerService,
        CityManagerService,
        StopService,
        DepartureService,
        RouteService,
        TimetableService,
        OtpService,
        GbfsService,
        {
            provide: STEPPER_GLOBAL_OPTIONS,
            useValue: {displayDefaultIndicatorType: true}
        }
        // {
        //   provide: STEPPER_GLOBAL_OPTIONS,
        //   useValue: { displayDefaultIndicatorType: false }
        // }
    ]
})
export class MapModule {
}
