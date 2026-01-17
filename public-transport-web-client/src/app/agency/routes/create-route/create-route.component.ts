import {Component, OnInit} from '@angular/core';
import {
    AgencyAddress,
    ModificationRouteResponse,
    Route, RouteId,
    RouteService, Stop
} from "../../../generated/public-transport-api";
import {Router} from "@angular/router";
import {NotificationService} from "../../../shared/notification.service";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {sampleSize, without} from "lodash";
import {take} from "rxjs";

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

    public modelForm: FormGroup;
    public isSubmitted: boolean = false;

    get originControl(): FormGroup {
        return this.modelForm.get('origin') as FormGroup;
    }

    get destinationControl(): FormGroup {
        return this.modelForm.get('destination') as FormGroup;
    }

    constructor(private _router: Router, private routeService: RouteService, private agencyStorageService: AgencyStorageService, private notificationService: NotificationService, private formBuilder: FormBuilder) {
        this.modelForm = this.formBuilder.group({
            line: ['', [Validators.required]],
            name: ['', [Validators.required]],
            google: [false],
            active: [true],
            origin: this.formBuilder.group({
                id: ['', Validators.required],
                name: [''],
                lon: [''],
                lat: ['']
            }),
            destination: this.formBuilder.group({
                id: ['', Validators.required],
                name: [''],
                lon: [''],
                lat: ['']
            })
        });
    }

    ngOnInit(): void {
        this.agencyStorageService.getAgencyAddress()
            .pipe(take(1))
            .subscribe((agencyAddress: AgencyAddress) => {
                if (agencyAddress && this.originControl && this.destinationControl) {
                    this.originControl.patchValue({
                        lon: agencyAddress.lon,
                        lat: agencyAddress.lat
                    });

                    this.destinationControl.patchValue({
                        lon: agencyAddress.lon,
                        lat: agencyAddress.lat
                    });
                }
            });
        this.randomConnection = this.getRandomConnection();
        this.randomLine = this.getRandomLine();
    }

    public createRouteAndNavigateToCreateNewTrip(): void {
        this.isSubmitted = true;

        if (this.modelForm.invalid) {
            return;
        }

        const routeId: RouteId = {};
        routeId.line = this.modelForm.get("line").value;
        routeId.name = this.modelForm.get("name").value;
        routeId.version = null;

        const originStop: Stop = {};
        originStop.id = this.originControl.get('id').value;

        const destinationStop: Stop = {};
        destinationStop.id = this.destinationControl.get('id').value;

        const createRouteRequest: Route = {};
        createRouteRequest.routeId = routeId;
        createRouteRequest.active = this.modelForm.get("active").value;
        createRouteRequest.google = this.modelForm.get("google").value;
        createRouteRequest.originStop = originStop;
        createRouteRequest.destinationStop = destinationStop;

        this.routeService.createRoute(this.agencyStorageService.getInstance(), createRouteRequest).subscribe((response: ModificationRouteResponse) => {
            this.notificationService.showSuccess(`Linia ${this.route.routeId.line} (${this.route.routeId.name}) została pomyślnie utworzona`);
            this._router.navigate(['/agency/trips/create'], {
                queryParams: {
                    line: response.routeId.line,
                    name: response.routeId.name,
                    version: response.routeId.version
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
