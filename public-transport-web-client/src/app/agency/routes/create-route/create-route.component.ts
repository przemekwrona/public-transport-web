import {Component, OnInit} from '@angular/core';
import {ModificationRouteResponse, Route, RouteService, Status, Stop} from "../../../generated/public-transport-api";
import {Router} from "@angular/router";
import {BusStopSelectorData} from "../../shared/bus-stop-selector/bus-stop-selector.component";
import {NotificationService} from "../../../shared/notification.service";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {FormUtils} from "../../../shared/form.utils";
import {pull, remove, sampleSize, without} from "lodash";

@Component({
    selector: 'app-create-route',
    templateUrl: './create-route.component.html',
    styleUrl: './create-route.component.scss'
})
export class CreateRouteComponent implements OnInit {

    private randomCities: string[] = ["Kielce", "Warszawa", "Kraków", "Poznań", "Wrocław", "Opole"]
    private randomLines: string[] = ["L1", "1", "201", "28", "P2"]

    public randomConnection: string = '';
    public randomLine: string = '';

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
            line: ['', [Validators.required]],
            name: ['', [Validators.required]],
            google: [false],
            active: [true],
            origin: [this.formBuilder.group({
                id: [''],
                name: [''],
                lon: [''],
                lat: ['']
            }), [Validators.required]],
            destination: [this.formBuilder.group({
                id: [''],
                name: [''],
                lon: [''],
                lat: ['']
            }), [Validators.required]]
        });

        this.randomConnection = this.getRandomConnection();
        this.randomLine = this.getRandomLine();
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

    public getRandomLine(): string {
        return sampleSize(this.randomLines, 1)[0];
    }

    public getRandomConnection(): string {
        const from: string = sampleSize(this.randomCities, 1)[0];
        const citiesWithoutFrom: string[] = without(this.randomCities, from);
        const destination: string = sampleSize(citiesWithoutFrom, 1)[0];

        return `${from} - ${destination}`;
    }

    public getControl(control: string): FormControl {
        return this.modelForm.get(control) as FormControl;
    }

    public getLineControl(): FormControl<string> {
        return this.getControl("line") as FormControl<string>;
    }

    public getNameControl(): FormControl<string> {
        return this.getControl("name") as FormControl<string>;
    }

}
