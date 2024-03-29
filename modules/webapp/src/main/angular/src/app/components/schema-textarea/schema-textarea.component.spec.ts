import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SchemaTextareaComponent} from './schema-textarea.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule} from "@angular/forms";
import {AppConfigService} from "../../services/app-config.service";
import {APP_BASE_HREF} from "@angular/common";

describe('SchemaTextareaComponent', () => {
  let component: SchemaTextareaComponent;
  let fixture: ComponentFixture<SchemaTextareaComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SchemaTextareaComponent],
      imports: [HttpClientTestingModule, FormsModule],
      providers: [
        AppConfigService,
        {
          provide: APP_BASE_HREF,
          useValue: "/"
        }
      ]
    });
    fixture = TestBed.createComponent(SchemaTextareaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
