import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ItineraryDescriptionComponent } from './itinerary-description.component';

describe('ItineraryDescriptionComponent', () => {
  let component: ItineraryDescriptionComponent;
  let fixture: ComponentFixture<ItineraryDescriptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItineraryDescriptionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ItineraryDescriptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
