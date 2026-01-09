import {Component, OnInit} from '@angular/core';
import {ModificationRouteResponse, Route, RouteService, Status, Stop} from "../../../generated/public-transport-api";
import {Router} from "@angular/router";
import {BusStopSelectorData} from "../../shared/bus-stop-selector/bus-stop-selector.component";
import {NotificationService} from "../../../shared/notification.service";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {FormUtils} from "../../../shared/form.utils";

@Component({
    selector: 'app-create-route',
    templateUrl: './create-route.component.html',
    styleUrl: './create-route.component.scss'
})
export class CreateRouteComponent implements OnInit {

    public route: Route = {
        routeId: {
            line: "",
            name: "",
            version: null
        },
        google: false,
        active: true
    };

    public origin: BusStopSelectorData = {} as BusStopSelectorData;
    public destination: BusStopSelectorData = {} as BusStopSelectorData;
    public modelForm: FormGroup;
    public isSubmitted: boolean = false;

    constructor(private _router: Router, private routeService: RouteService, private agencyStorageService: AgencyStorageService, private notificationService: NotificationService, private formBuilder: FormBuilder) {
    }

    ngOnInit(): void {
        this.agencyStorageService.getAgencyAddress().subscribe(agencyAddress => {
            this.origin.stopLon = agencyAddress.lon;
            this.origin.stopLat = agencyAddress.lat;

            this.destination.stopLon = agencyAddress.lon;
            this.destination.stopLat = agencyAddress.lat;
        });

        this.modelForm = this.formBuilder.group({
            line: ['', []],
            name: ['', [Validators.required]],
            google: [false],
            active: [true],
            origin: ['', [Validators.required]],
            destination: ['', [Validators.required]]
        });
    }

    public createRouteAndNavigateToCreateNewTrip(): void {
        this.isSubmitted = true;
        console.log(FormUtils.getFormValidationErrors(this.modelForm));
        if (this.modelForm.invalid) {
            return;
        }

        const originStop: Stop = {} as Stop;
        originStop.id = this.origin.stopId;
        originStop.name = this.origin.stopName

        const destinationStop: Stop = {} as Stop;
        destinationStop.id = this.destination.stopId;
        destinationStop.name = this.destination.stopName;

        this.route.originStop = originStop;
        this.route.destinationStop = destinationStop;

        this.routeService.createRoute(this.agencyStorageService.getInstance(), this.route).subscribe((response: ModificationRouteResponse) => {
            this.notificationService.showSuccess(`Linia ${this.route.routeId.line} (${this.route.routeId.name}) została pomyślnie utworzona`);
            this._router.navigate(['/agency/trips/create'], {
                queryParams: {
                    line: response.routeId.line,
                    name: response.routeId.name
                }
            }).then();
        });
    }


}
