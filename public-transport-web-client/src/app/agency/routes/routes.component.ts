import {AfterViewInit, ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {LeafletEvent, LeafletMouseEvent, Map, Marker} from "leaflet";
import * as L from "leaflet";
import {StopService} from "../stops/stop.service";
import {Routes, Stop} from "../../generated/public-transport";
import {ActivatedRoute, Router} from "@angular/router";

enum RouteComponentMode {
    OVERVIEW,
    EDIT
}

@Component({
    selector: 'app-routes',
    templateUrl: './routes.component.html',
    styleUrl: './routes.component.scss',
    changeDetection: ChangeDetectionStrategy.Default
})
export class RoutesComponent implements OnInit {
    public stops: Stop[] = [];

    public routes: Routes = {};

    constructor(private stopService: StopService, private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.routes = this.route.snapshot.data['routes'];
    }

    public clickAddRoute(): void {
        this.router.navigate(['/agency/routes/create']);
    }

}
