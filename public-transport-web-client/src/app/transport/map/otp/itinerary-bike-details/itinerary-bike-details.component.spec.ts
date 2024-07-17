import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ItineraryBikeDetailsComponent } from './itinerary-bike-details.component';

describe('ItineraryBikeDetailsComponent', () => {
  let component: ItineraryBikeDetailsComponent;
  let fixture: ComponentFixture<ItineraryBikeDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItineraryBikeDetailsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ItineraryBikeDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
