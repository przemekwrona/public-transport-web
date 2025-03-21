import {Component} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {GetBrigadeResponse} from "../../../generated/public-transport";

@Component({
    selector: 'app-brigade-list',
    templateUrl: './brigade-list.component.html',
    styleUrl: './brigade-list.component.scss'
})
export class BrigadeListComponent {

    public brigadesResponse: GetBrigadeResponse;

    constructor(private route: ActivatedRoute) {
        this.brigadesResponse = this.route.snapshot.data['brigades'];
    }

}
