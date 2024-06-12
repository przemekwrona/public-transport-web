import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ItineraryTransitComponent } from './itinerary-transit.component';

describe('ItineraryTransitComponent', () => {
  let component: ItineraryTransitComponent;
  let fixture: ComponentFixture<ItineraryTransitComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItineraryTransitComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ItineraryTransitComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
