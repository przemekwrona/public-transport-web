import {Component} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {BrigadeDeleteBody, BrigadeService, GetBrigadeResponse} from "../../../generated/public-transport";
import {size} from "lodash";

@Component({
    selector: 'app-brigade-list',
    templateUrl: './brigade-list.component.html',
    styleUrl: './brigade-list.component.scss'
})
export class BrigadeListComponent {

    public brigadesResponse: GetBrigadeResponse;

    constructor(private route: ActivatedRoute, private brigadeService: BrigadeService) {
        this.brigadesResponse = this.route.snapshot.data['brigades'];
    }

    public deleteBrigadeByName(brigadeName: string): void {
        const brigadeDeleteBody = {} as BrigadeDeleteBody;
        brigadeDeleteBody.brigadeName = brigadeName;

        this.brigadeService.deleteBrigade(brigadeDeleteBody).subscribe(() => {
            this.brigadeService.getBrigades().subscribe(response => this.brigadesResponse = response);
        });
    }

    public hasBrigades(): boolean {
        return size(this.brigadesResponse.brigades) > 0;
    }

}
