import {Component, OnInit} from '@angular/core';
import {BrigadeService} from "../brigade.service";
import {GetAllTripsResponse} from "../../../generated/public-transport";

@Component({
  selector: 'app-brigade-editor',
  templateUrl: './brigade-editor.component.html',
  styleUrl: './brigade-editor.component.scss'
})
export class BrigadeEditorComponent implements OnInit {

  public trips: GetAllTripsResponse = {};

  constructor(private brigadeService: BrigadeService) {
  }

  ngOnInit(): void {
    this.brigadeService.getRoutes('').subscribe((response: GetAllTripsResponse) => this.trips = response);
  }

  protected readonly JSON = JSON;
}
